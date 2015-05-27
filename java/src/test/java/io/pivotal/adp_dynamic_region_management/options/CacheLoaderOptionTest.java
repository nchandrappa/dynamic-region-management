package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.CacheSingleton;
import io.pivotal.adp_dynamic_region_management.FakeCacheLoader;
import io.pivotal.adp_dynamic_region_management.RegionOptionsFactory;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

public class CacheLoaderOptionTest {
    @BeforeClass
    static public void setUp() throws Exception {
        CacheSingleton.getCache();
    }

    @Rule
    public TestName name = new TestName();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testCacheLoader() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"cacheLoader\": \"io.pivotal.adp_dynamic_region_management.FakeCacheLoader\" }");

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);

        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();
        Region region = regionFactory.create(getCurrentTestName());

        new CacheLoaderOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        assertEquals(FakeCacheLoader.class, region.getAttributes().getCacheLoader().getClass());
    }

    @Test
    public void testCacheLoaderWithInvalidClassName() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"cacheLoader\": \"does.not.exist.FakeCacheLoader\" }");

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);
        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Invalid class name: `does.not.exist.FakeCacheLoader`.");

        new CacheLoaderOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testCacheLoaderWithNonCacheLoaderClassName() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"cacheLoader\": \"java.lang.Integer\" }");

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);
        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Unable to instantiate: `java.lang.Integer`.");

        new CacheLoaderOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testIsAnOptionReturnsFalse() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{}");
        assertThat(new CacheLoaderOption(serverOptions).isAnOption(), equalTo(false));
    }

    @Test
    public void testIsAnOptionReturnsTrue() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{\"cacheLoader\": null}");
        assertThat(new CacheLoaderOption(serverOptions).isAnOption(), equalTo(true));
    }
    private String getCurrentTestName() {
        return getClass().getSimpleName() + name.getMethodName();
    }

}
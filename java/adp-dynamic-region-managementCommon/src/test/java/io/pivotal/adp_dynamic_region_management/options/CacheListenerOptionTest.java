package io.pivotal.adp_dynamic_region_management.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import io.pivotal.adp_dynamic_region_management.CacheSingleton;
import io.pivotal.adp_dynamic_region_management.FakeCacheListener;
import io.pivotal.adp_dynamic_region_management.RegionOptionsFactory;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;
import io.pivotal.adp_dynamic_region_management.options.CacheListenerOption;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;

public class CacheListenerOptionTest {
    @BeforeClass
    static public void setUp() throws Exception {
        CacheSingleton.getCache();
    }

    @Rule
    public TestName name = new TestName();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testCacheListener() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"cacheListener\": [\"io.pivotal.adp_dynamic_region_management.FakeCacheListener\"] }");

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);

        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();
        Region region = regionFactory.create(getCurrentTestName());

        new CacheListenerOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        assertEquals(FakeCacheListener.class, region.getAttributes().getCacheListeners()[0].getClass());
    }

    @Test
    public void testCacheListenerWithInvalidClassName() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"cacheListener\": [\"does.not.exist.FakeCacheListener\"] }");

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);
        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Invalid class name: `does.not.exist.FakeCacheListener`.");

        new CacheListenerOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testCacheListenerWithNonCacheListenerClassName() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"cacheListener\": [\"java.lang.Integer\"] }");

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);
        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Unable to instantiate: `java.lang.Integer`.");

        new CacheListenerOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testIsAnOptionReturnsFalse() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{}");
        assertThat(new CacheListenerOption(serverOptions).isAnOption(), equalTo(false));
    }

    @Test
    public void testIsAnOptionReturnsTrue() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{\"cacheListener\": null}");
        assertThat(new CacheListenerOption(serverOptions).isAnOption(), equalTo(true));
    }
    private String getCurrentTestName() {
        return getClass().getSimpleName() + name.getMethodName();
    }

}
package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.CacheSingleton;
import io.pivotal.adp_dynamic_region_management.FakeCacheWriter;
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

public class CacheWriterOptionTest {
    @BeforeClass
    static public void setUp() throws Exception {
        CacheSingleton.getCache();
    }

    @Rule
    public TestName name = new TestName();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testCacheWriter() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"cacheWriter\": \"io.pivotal.adp_dynamic_region_management.FakeCacheWriter\" }");

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);

        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();
        Region region = regionFactory.create(getCurrentTestName());

        new CacheWriterOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        assertEquals(FakeCacheWriter.class, region.getAttributes().getCacheWriter().getClass());
    }

    @Test
    public void testCacheWriterWithInvalidClassName() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"cacheWriter\": \"does.not.exist.FakeCacheWriter\" }");

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);
        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Invalid cacheWriter class name: `does.not.exist.FakeCacheWriter`.");

        new CacheWriterOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testCacheWriterWithNonCacheWriterClassName() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"cacheWriter\": \"java.lang.String\" }");

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);
        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Unable to instantiate cacheWriter: `java.lang.String`.");

        new CacheWriterOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testIsAnOptionReturnsFalse() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{}");
        assertThat(new CacheWriterOption(serverOptions).isAnOption(), equalTo(false));
    }

    @Test
    public void testIsAnOptionReturnsTrue() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{\"cacheWriter\": null}");
        assertThat(new CacheWriterOption(serverOptions).isAnOption(), equalTo(true));
    }
    private String getCurrentTestName() {
        return getClass().getSimpleName() + name.getMethodName();
    }

}
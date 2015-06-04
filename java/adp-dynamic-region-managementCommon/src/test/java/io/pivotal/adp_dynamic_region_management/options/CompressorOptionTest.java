package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;

import io.pivotal.adp_dynamic_region_management.CacheSingleton;
import io.pivotal.adp_dynamic_region_management.FakeCompressor;
import io.pivotal.adp_dynamic_region_management.RegionOptionsFactory;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;
import io.pivotal.adp_dynamic_region_management.options.CompressorOption;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

public class CompressorOptionTest {
    @BeforeClass
    static public void setUp() throws Exception {
        CacheSingleton.getCache();
    }

    @Rule
    public TestName name = new TestName();

    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    @Test
    public void testCompressor() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"compressor\": \"io.pivotal.adp_dynamic_region_management.FakeCompressor\" }");

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);

        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();
        Region region = regionFactory.create(getCurrentTestName());

        new CompressorOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        assertEquals(FakeCompressor.class, region.getAttributes().getCompressor().getClass());
    }

    @Test
    public void testCompressorWithInvalidClassName() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"compressor\": \"does.not.exist.FakeCompressor\" }");

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);
        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Invalid class name: `does.not.exist.FakeCompressor`.");

        new CompressorOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testCompressorWithNonCompressorClassName() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"compressor\": \"java.lang.Integer\" }");

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);
        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Unable to instantiate: `java.lang.Integer`.");

        new CompressorOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testIsAnOptionReturnsFalse() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{}");
        assertThat(new CompressorOption(serverOptions).isAnOption(), equalTo(false));
    }

    @Test
    public void testIsAnOptionReturnsTrue() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{\"compressor\": null}");
        assertThat(new CompressorOption(serverOptions).isAnOption(), equalTo(true));
    }
    private String getCurrentTestName() {
        return getClass().getSimpleName() + name.getMethodName();
    }
}
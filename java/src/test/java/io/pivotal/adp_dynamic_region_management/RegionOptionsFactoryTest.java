package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class RegionOptionsFactoryTest {
    @BeforeClass
    static public void setUp() throws Exception {
        CacheSingleton.getCache();
    }

    @Rule
    public TestName name = new TestName();

    @Test
    public void testGetRegionFactoryWithEmptyOptions() throws Exception {
        String jsonString = "{}";
        PdxInstance serverOptions = JSONFormatter.fromJSON(jsonString);

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);
        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();
        Region region = regionFactory.create(getCurrentTestName());

        assertThat(region.getAttributes().getDataPolicy(), equalTo(DataPolicy.DEFAULT));
    }

    private String getCurrentTestName() {
        return name.getMethodName();
    }

}
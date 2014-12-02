package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.CacheSingleton;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class IndexUpdateTypeOptionTest {
    private static RegionFactory regionFactory;
    private static PdxInstance serverOptions;

    @BeforeClass
    static public void setUp() throws Exception {
        CacheSingleton.getCache();

        regionFactory = CacheFactory.getAnyInstance().createRegionFactory();
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TestName name = new TestName();

    @Test
    public void testGetRegionFactoryWithIndexUpdateTypeSynchronous() throws Exception {
        serverOptions = JSONFormatter.fromJSON("{ \"indexUpdateType\": \"synchronous\" }");
        new IndexUpdateTypeOption(serverOptions).setOptionOnRegionFactory(regionFactory);
        Region region = regionFactory.create(getCurrentTestName());

        assertThat(region.getAttributes().getIndexMaintenanceSynchronous(), equalTo(true));
    }

    @Test
    public void testGetRegionFactoryWithIndexUpdateTypeAsynchronous() throws Exception {
        serverOptions = JSONFormatter.fromJSON("{ \"indexUpdateType\": \"asynchronous\" }");
        new IndexUpdateTypeOption(serverOptions).setOptionOnRegionFactory(regionFactory);
        Region region = regionFactory.create(getCurrentTestName());

        assertThat(region.getAttributes().getIndexMaintenanceSynchronous(), equalTo(false));
    }

    @Test
    public void testValidateWithInvalidIndexUpdateType() throws Exception {
        serverOptions = JSONFormatter.fromJSON("{ \"indexUpdateType\": \"invalid\" }");

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Invalid indexUpdateType `invalid`, valid values are `synchronous` or `asynchronous`.");

        new IndexUpdateTypeOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    private String getCurrentTestName() {
        return name.getMethodName();
    }
}
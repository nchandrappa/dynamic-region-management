package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;

import io.pivotal.adp_dynamic_region_management.CacheSingleton;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;
import io.pivotal.adp_dynamic_region_management.options.DataPolicyOption;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DataPolicyOptionTest {
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
    public void testValidateBadDataPolicy() throws Exception {
        serverOptions = JSONFormatter.fromJSON("{ \"dataPolicy\": \"FOO\" }");

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Invalid dataPolicy: `FOO`.");

        new DataPolicyOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testValidateGoodDataPolicy() throws Exception {
        serverOptions = JSONFormatter.fromJSON("{ \"dataPolicy\": \"REPLICATE\" }");

        new DataPolicyOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        assertThat(region.getAttributes().getDataPolicy().toString(), equalTo("REPLICATE"));
    }

    private String getCurrentTestName() {
        return getClass().getSimpleName() + name.getMethodName();
    }
}
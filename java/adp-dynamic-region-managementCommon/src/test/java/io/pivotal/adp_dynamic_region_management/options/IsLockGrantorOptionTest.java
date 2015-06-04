package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.Scope;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;

import io.pivotal.adp_dynamic_region_management.CacheSingleton;
import io.pivotal.adp_dynamic_region_management.options.IsLockGrantorOption;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class IsLockGrantorOptionTest {
    private static RegionFactory regionFactory;
    private static PdxInstance serverOptions;

    @BeforeClass
    static public void setUp() throws Exception {
        CacheSingleton.getCache();

        regionFactory = CacheFactory.getAnyInstance().createRegionFactory();
        serverOptions = JSONFormatter.fromJSON("{ \"isLockGrantor\": true }");
    }

    @Rule
    public TestName name = new TestName();

    @Test
    public void testGetRegionFactoryWithIsLockGrantor() throws Exception {
        new IsLockGrantorOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        regionFactory.setScope(Scope.GLOBAL);
        Region region = regionFactory.create(getCurrentTestName());

        assertThat(region.getAttributes().isLockGrantor(), equalTo(true));
    }

    private String getCurrentTestName() {
        return getClass().getSimpleName() + name.getMethodName();
    }
}
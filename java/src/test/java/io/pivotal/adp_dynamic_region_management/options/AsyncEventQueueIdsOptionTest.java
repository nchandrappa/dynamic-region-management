package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.CacheSingleton;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

public class AsyncEventQueueIdsOptionTest {

    private static RegionFactory regionFactory;
    private static PdxInstance serverOptions;

    @BeforeClass
    static public void setUp() throws Exception {
        CacheSingleton.getCache();

        regionFactory = CacheFactory.getAnyInstance().createRegionFactory();
        serverOptions = JSONFormatter.fromJSON("{ \"asyncEventQueueIds\": [\"queue1\", \"queue2\"] }");
    }

    @Rule
    public TestName name = new TestName();

    @Test
    public void testGetRegionFactoryWithAsyncEventQueueIds() throws Exception {
        new AsyncEventQueueIdsOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        Set<String> asyncEventQueueIds = region.getAttributes().getAsyncEventQueueIds();
        assertThat(asyncEventQueueIds, hasSize(2));
        assertThat(asyncEventQueueIds, contains("queue1", "queue2"));
    }

    private String getCurrentTestName() {
        return name.getMethodName();
    }
}

package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateRegionCacheListenerTest {

    static private Cache cache;

    @BeforeClass
    static public void setUp() throws Exception {
        CacheFactory cacheFactory = new CacheFactory();
        cacheFactory.set("locators", "");
        cacheFactory.set("mcast-port", "0");
        cache = cacheFactory.create();
    }

    @Mock
    EntryEvent<String, PdxInstance> event;

    @Rule
    public TestName name = new TestName();

    @Test
    public void testAfterCreatePassesPartitionTypeToRegion() throws Exception {
        String jsonString = "{\n" +
                "    \"server\": {\n" +
                "        \"type\": \"PARTITION\"\n" +
                "    }\n" +
                "}";
        PdxInstance regionConfig = JSONFormatter.fromJSON(jsonString);

        when(event.getKey()).thenReturn(getCurrentTestName());
        when(event.getNewValue()).thenReturn(regionConfig);

        CreateRegionCacheListener listener = new CreateRegionCacheListener();

        listener.afterCreate(event);

        Region region = cache.getRegion(getCurrentTestName());

        assertThat(region.getAttributes().getDataPolicy(), equalTo(DataPolicy.PARTITION));
    }

    @Test
    public void testAfterCreatePassesDefaultTypeToRegionWhenServerIsNotPresent() throws Exception {
        String jsonString = "{}";
        PdxInstance regionConfig = JSONFormatter.fromJSON(jsonString);

        when(event.getKey()).thenReturn(getCurrentTestName());
        when(event.getNewValue()).thenReturn(regionConfig);

        CreateRegionCacheListener listener = new CreateRegionCacheListener();

        listener.afterCreate(event);

        Region region = cache.getRegion(getCurrentTestName());

        assertThat(region.getAttributes().getDataPolicy(), equalTo(DataPolicy.NORMAL));
    }

    @Test
    public void testAfterCreatePassesDefaultTypeToRegionWhenServerHasNoType() throws Exception {
        String jsonString = "{\"server\": {}}";
        PdxInstance regionConfig = JSONFormatter.fromJSON(jsonString);

        when(event.getKey()).thenReturn(getCurrentTestName());
        when(event.getNewValue()).thenReturn(regionConfig);

        CreateRegionCacheListener listener = new CreateRegionCacheListener();

        listener.afterCreate(event);

        Region region = cache.getRegion(getCurrentTestName());

        assertThat(region.getAttributes().getDataPolicy(), equalTo(DataPolicy.NORMAL));
    }

    private String getCurrentTestName() {
        return name.getMethodName();
    }
}
package io.pivotal.adp_dynamic_region_management;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;

/**
 *<P>Tests that use a real Gemfire cache.</P>
 *<P>See {@link io.pivotal.adp_dynamic_region_management.MetadataRegionCacheListenerMockTest}
 *for ones where the cache is faked to give specific errors that rarely occur.</P>
 */
@RunWith(MockitoJUnitRunner.class)
public class MetadataRegionCacheListenerTest {

    static private Cache cache;

    @BeforeClass
    static public void setUp() throws Exception {
        cache = CacheSingleton.getCache();
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

        MetadataRegionCacheListener listener = new MetadataRegionCacheListener();

        listener.afterCreate(event);

        Region<?,?> region = cache.getRegion(getCurrentTestName());

        assertThat(region.getAttributes().getDataPolicy(), equalTo(DataPolicy.PARTITION));
    }

    @Test
    public void testAfterCreatePassesDefaultTypeToRegionWhenServerIsIncomplete() throws Exception {
        String jsonString = "{ \"server\": {}  }";

        PdxInstance regionConfig = JSONFormatter.fromJSON(jsonString);

        when(event.getKey()).thenReturn(getCurrentTestName());
        when(event.getNewValue()).thenReturn(regionConfig);

        MetadataRegionCacheListener listener = new MetadataRegionCacheListener();

        listener.afterCreate(event);

        Region<?,?> region = cache.getRegion(getCurrentTestName());

        assertThat(region.getAttributes().getDataPolicy(), equalTo(DataPolicy.NORMAL));
    }

    @Test
    public void testAfterCreatePassesDefaultTypeToRegionWhenServerHasNoType() throws Exception {
        String jsonString = "{\"server\": {}}";
        PdxInstance regionConfig = JSONFormatter.fromJSON(jsonString);

        when(event.getKey()).thenReturn(getCurrentTestName());
        when(event.getNewValue()).thenReturn(regionConfig);

        MetadataRegionCacheListener listener = new MetadataRegionCacheListener();

        listener.afterCreate(event);

        Region<?,?> region = cache.getRegion(getCurrentTestName());

        assertThat(region.getAttributes().getDataPolicy(), equalTo(DataPolicy.NORMAL));
    }

    @Test
    public void testAfterUpdateAppliesUpdatesToRegion() throws Exception {
        String regionName = getCurrentTestName();
        String jsonString =
                "{" +
                        "  \"server\": {" +
                        "    \"cloningEnabled\": true" +
                        "  }" +
                        "}";
        PdxInstance regionConfig = JSONFormatter.fromJSON(jsonString);

        Region<?,?> region = createRegion(regionName);

        when(event.getKey()).thenReturn(getCurrentTestName());
        when(event.getNewValue()).thenReturn(regionConfig);

        MetadataRegionCacheListener listener = new MetadataRegionCacheListener();

        assertThat(region.getAttributes().getCloningEnabled(), equalTo(false));

        listener.afterUpdate(event);

        assertThat(region.getAttributes().getCloningEnabled(), equalTo(true));
    }

    @Test
    public void testAfterDestroy() throws Exception {
        String jsonString = "{\"server\": {}}";
        PdxInstance regionConfig = JSONFormatter.fromJSON(jsonString);

        when(event.getKey()).thenReturn(getCurrentTestName());
        when(event.getNewValue()).thenReturn(regionConfig);

        MetadataRegionCacheListener listener = new MetadataRegionCacheListener();

        listener.afterCreate(event);

        Region<?,?> region = cache.getRegion(getCurrentTestName());
        assertNotNull("Must successfully create before destroying", region);

        /* Destroy can be run twice, once when present and again once absent
         */
        for(int i=0; i<2; i++) {
            listener.afterDestroy(event);
            
            region = cache.getRegion(getCurrentTestName());
            assertNull("After destroy i==" + i, region);
        }
    }

    private Region<?,?> createRegion(String name) {
        Region<String, PdxInstance> metadataRegion = MetadataRegion.getMetadataRegion();
        PdxInstance regionOptions = JSONFormatter.fromJSON("{ " +
        		" \"client\" : { \"type\": \"CACHING_PROXY\" }"  +
        		"," +
        		" \"server\" : { }"  +
        		"}");
        metadataRegion.put(name, regionOptions);
        // region is created by the CacheListener
        Region<?,?> region = cache.getRegion(name);
        assertThat(region, notNullValue());
        assertThat(metadataRegion.containsKey(name), Matchers.equalTo(true));
        return region;
    }


    private String getCurrentTestName() {
        return getClass().getSimpleName() + name.getMethodName();
    }
}
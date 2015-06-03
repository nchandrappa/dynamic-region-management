package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.ResultSender;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateRegionTest {
    @Rule
    public TestName name = new TestName();

    public String regionName;

    public Cache cache;

    @Mock
    FunctionContext context;

    @Mock
    ResultSender<Object> resultSender;

    @Before
    public void setUp() {
    	regionName = getClass().getSimpleName() + name.getMethodName();
        cache = CacheSingleton.getCache();
        GemfireFunctionHelper.rethrowFunctionExceptions(resultSender);
    }

    @Test
    public void hasResult() throws Exception {
        assertThat(new UpdateRegion().hasResult(), equalTo(true));
    }

    @Test
    public void getId() throws Exception {
        assertThat(new UpdateRegion().getId(), equalTo("UpdateRegion"));
    }

    @Test
    public void testIsHA() throws Exception {
        assertThat(new UpdateRegion().isHA(), equalTo(false));
    }

    @Test
    public void executeSendsTrueResultOnSuccess() throws Exception {
        createRegion(regionName);
        when(context.getResultSender()).thenReturn(resultSender);

        PdxInstance regionOptions = JSONFormatter.fromJSON(
                "{" +
                        "  \"server\": {" +
                        "    \"cloningEnabled\": true" +
                        "  }" +
                        "}");

        when(context.getArguments()).thenReturn(Arrays.asList(regionName, regionOptions));

        new UpdateRegion().execute(context);

        verify(resultSender).lastResult(true);
    }

    @Test
    public void executeSendsFalseResultWhenRegionDoesNotExist() throws Exception {
        when(context.getResultSender()).thenReturn(resultSender);

        PdxInstance regionOptions = JSONFormatter.fromJSON(
                "{" +
                        "  \"server\": {" +
                        "    \"cloningEnabled\": true" +
                        "  }" +
                        "}");

        when(context.getArguments()).thenReturn(Arrays.asList(regionName, regionOptions));

        new UpdateRegion().execute(context);

        verify(resultSender).lastResult(false);
    }

    @Test
    public void executeUpdatesTheRegionsSettings() throws Exception {
        createRegion(regionName);
        when(context.getResultSender()).thenReturn(resultSender);

        PdxInstance regionOptions = JSONFormatter.fromJSON(
                "{" +
                        "  \"server\": {" +
                        "    \"cloningEnabled\": true" +
                        "  }" +
                        "}");
        when(context.getArguments()).thenReturn(Arrays.asList(regionName, regionOptions));

        new UpdateRegion().execute(context);

        Region<Object,Object> region = cache.getRegion(regionName);
        assertThat(region.getAttributes().getCloningEnabled(), equalTo(true));
    }

    @Test
    public void executeUpdatesTheRegionsMetadataObject() throws Exception {
        createRegion(regionName);
        when(context.getResultSender()).thenReturn(resultSender);

        PdxInstance regionOptions = JSONFormatter.fromJSON(
                "{" +
                        "  \"server\": {" +
                        "    \"cloningEnabled\": true" +
                        "  }" +
                        "}");
        when(context.getArguments()).thenReturn(Arrays.asList(regionName, regionOptions));

        new UpdateRegion().execute(context);

        Region<String,PdxInstance> metadataRegion = MetadataRegion.getMetadataRegion();

        PdxInstance updatedRegionOptions = (PdxInstance) metadataRegion.get(regionName);
        PdxInstance updatedServerOptions = (PdxInstance) updatedRegionOptions.getField("server");
        assertThat((Boolean) updatedServerOptions.getField("cloningEnabled"), equalTo(true));
    }

    @Test
    public void executeUpdatesTheRegionsMetadataObjectWhenSettingsAreNotAlreadyPresent() throws Exception {
        PdxInstance originalRegionOptions = JSONFormatter.fromJSON(
                "{" +
                        "  \"client\": {" +
                        "    \"type\": \"CACHING_PROXY\"" +
                        "  }," +
                        "  \"server\": {" +
                        "    \"dataPolicy\": \"NORMAL\"" +
                        "  }" +
                        "}");
        createRegion(regionName, originalRegionOptions);

        when(context.getResultSender()).thenReturn(resultSender);

        PdxInstance regionOptions = JSONFormatter.fromJSON(
                "{" +
                        "  \"server\": {" +
                        "    \"cloningEnabled\": true" +
                        "  }" +
                        "}");
        when(context.getArguments()).thenReturn(Arrays.asList(regionName, regionOptions));

        new UpdateRegion().execute(context);

        Region<String,PdxInstance> metadataRegion = MetadataRegion.getMetadataRegion();

        PdxInstance updatedRegionOptions = (PdxInstance) metadataRegion.get(regionName);
        PdxInstance updatedServerOptions = (PdxInstance) updatedRegionOptions.getField("server");
        assertThat((Boolean) updatedServerOptions.getField("cloningEnabled"), equalTo(true));
        assertThat((String) updatedServerOptions.getField("dataPolicy"), equalTo("NORMAL"));
    }

    @Test
    public void executeDoesNotUpdateTheRegionsMetadataObjectWhenServerChangesAreNotProvided() throws Exception {
        PdxInstance originalRegionOptions = JSONFormatter.fromJSON(
                "{" +
                        "  \"client\": {" +
                        "    \"type\": \"CACHING_PROXY\"" +
                        "  }" +
                        " , " + 
                        "  \"server\": {" +
                        "  }" +
                        "}");
        createRegion(regionName, originalRegionOptions);

        when(context.getResultSender()).thenReturn(resultSender);

        PdxInstance regionOptions = JSONFormatter.fromJSON(
                "{" +
                        "  \"server\": {" +
                        "    \"cloningEnabled\": true" +
                        "  }" +
                        "}");
        when(context.getArguments()).thenReturn(Arrays.asList(regionName, regionOptions));

        new UpdateRegion().execute(context);

        Region<String,PdxInstance> metadataRegion = MetadataRegion.getMetadataRegion();

        PdxInstance updatedRegionOptions = (PdxInstance) metadataRegion.get(regionName);
        PdxInstance updatedServerOptions = (PdxInstance) updatedRegionOptions.getField("server");
        assertThat((Boolean) updatedServerOptions.getField("cloningEnabled"), equalTo(true));
    }

    @Test
    public void updatesEntryIdleTimeout() throws Exception {
        PdxInstance originalRegionOptions = JSONFormatter.fromJSON(
                "{" +
                        "  \"client\": {" +
                        "    \"type\": \"CACHING_PROXY\"" +
                        "  }," +
                        "  \"server\": {" +
                        "    \"concurrencyChecksEnabled\": false, " +
                        "    \"isLockGrantor\": true, " +
                        "    \"scope\": \"GLOBAL\"," +
                        "    \"regionTimeToLive\": {\"timeout\": 1234}, " +
                        "    \"regionIdleTime\": {\"timeout\": 2345}, " +
                        "    \"entryTimeToLive\": {\"timeout\": 3456}, " +
                        "    \"entryIdleTime\": {\"timeout\": 4567} " +
                        "  }" +
                        "}");
        createRegion(regionName, originalRegionOptions);

        when(context.getResultSender()).thenReturn(resultSender);

        PdxInstance regionOptions = JSONFormatter.fromJSON(
                "{" +
                        "  \"server\": {" +
                        "    \"entryIdleTime\": {" +
                        "       \"action\": \"DESTROY\", " +
                        "       \"timeout\": 12345" +
                        "     }" +
                        "  }" +
                        "}");
        when(context.getArguments()).thenReturn(Arrays.asList(regionName, regionOptions));

        new UpdateRegion().execute(context);

        Region metadataRegion = MetadataRegion.getMetadataRegion();

        PdxInstance updatedRegionOptions = (PdxInstance) metadataRegion.get(regionName);
        PdxInstance updatedServerOptions = (PdxInstance) updatedRegionOptions.getField("server");
        PdxInstance entryIdleTime = (PdxInstance) updatedServerOptions.getField("entryIdleTime");
        assertThat((String) entryIdleTime.getField("action"), equalTo("DESTROY"));
        assertThat((Integer) entryIdleTime.getField("timeout"), equalTo(12345));
    }

    private void createRegion(String name) {
        PdxInstance regionOptions = JSONFormatter.fromJSON(
                "{" +
                        "  \"client\": {" +
                        "    \"type\": \"CACHING_PROXY\"" +
                        "  }," +
                        "  \"server\": {" +
                        "    \"cloningEnabled\": false" +
                        "  }" +
                        "}");
        createRegion(name, regionOptions);
    }

    private void createRegion(String name, PdxInstance regionOptions) {
        Region<String, PdxInstance> metadataRegion = MetadataRegion.getMetadataRegion();
        metadataRegion.put(name, regionOptions);
        // region is created by the CacheListener
        assertThat(cache.getRegion(name), notNullValue());
        assertThat(metadataRegion.containsKey(name), equalTo(true));
    }
}
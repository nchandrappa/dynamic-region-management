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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DestroyRegionTest {
    @Rule
    public TestName name = new TestName();

    public String regionName;

    public Cache cache;

    @Mock
    FunctionContext context;

    @Mock
    ResultSender resultSender;

    @Before
    public void setUp() {
        regionName = getClass().getName() + name.getMethodName();
        cache = CacheSingleton.getCache();
        GemfireFunctionHelper.rethrowFunctionExceptions(resultSender);
    }

    @Test
    public void getId() throws Exception {
        assertThat(new DestroyRegion().getId(), equalTo("DestroyRegion"));
    }

    @Test
    public void hasResult() throws Exception {
        assertThat(new DestroyRegion().hasResult(), equalTo(true));
    }

    @Test
    public void executeSendsTrueResultOnSuccess() throws Exception {
        createRegion(regionName);
        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(regionName));

        new DestroyRegion().execute(context);

        verify(resultSender).lastResult(true);
    }

    @Test
    public void executeSendsFalseResultWhenRegionDoesNotExist() throws Exception {
        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(regionName));

        new DestroyRegion().execute(context);

        verify(resultSender).lastResult(false);
    }

    @Test
    public void executeDestroysTheRegion() throws Exception {
        createRegion(regionName);
        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(regionName));

        new DestroyRegion().execute(context);

        assertThat(cache.getRegion(regionName), equalTo(null));
    }

    @Test
    public void executeDoesNotDestroyTheRegionIfItDoesNotExist() throws Exception {
        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(regionName));

        new DestroyRegion().execute(context);

        assertThat(cache.getRegion(regionName), equalTo(null));
    }

    @Test
    public void executeSendsExceptions() throws Exception {
        RuntimeException exception = new RuntimeException("This is my message");

        ResultSender resultSender = mock(ResultSender.class);

        createRegion(regionName);
        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenThrow(exception);

        new DestroyRegion().execute(context);

        ArgumentCaptor<Exception> argument = ArgumentCaptor.forClass(Exception.class);
        verify(resultSender).sendException(argument.capture());

        assertThat(argument.getValue().getMessage(), equalTo(exception.getMessage()));
        assertThat(argument.getValue().getStackTrace(), equalTo(exception.getStackTrace()));
    }

    @Test
    public void executeDestroysTheRegionMetadataEntry() throws Exception {
        createRegion(regionName);

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(regionName));
        new DestroyRegion().execute(context);

        Region<String, PdxInstance> metadataRegion = MetadataRegion.getMetadataRegion();
        assertThat(metadataRegion.containsKey(regionName), equalTo(false));
    }

    private void createRegion(String name) {
        Region<String, PdxInstance> metadataRegion = MetadataRegion.getMetadataRegion();
        PdxInstance regionOptions = JSONFormatter.fromJSON("{ \"client\": { \"type\": \"CACHING_PROXY\" } }");
        metadataRegion.put(name, regionOptions);
        // region is created by the CacheListener
        assertThat(cache.getRegion(name), notNullValue());
        assertThat(metadataRegion.containsKey(name), equalTo(true));
    }
}
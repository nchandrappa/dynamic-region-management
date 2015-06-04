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
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.pivotal.adp_dynamic_region_management.DestroyRegion;
import io.pivotal.adp_dynamic_region_management.MetadataRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

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

    @SuppressWarnings("unchecked")
    @Test
    public void executeSendsExceptions() throws Exception {
        RuntimeException exception = new RuntimeException("This is my message");

		ResultSender<Object> resultSender = mock(ResultSender.class);

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

    @Test
    public void executeDestroysMetadataEvenIfRegionMissing() throws Exception {
        createRegion(regionName);
        cache.getRegion(regionName).destroyRegion();

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(regionName));
        new DestroyRegion().execute(context);

        Region<String, PdxInstance> metadataRegion = MetadataRegion.getMetadataRegion();
        assertThat(metadataRegion.containsKey(regionName), equalTo(false));
    }

    /* If no metadata entry, region wasn't created by Dynamic Region Management
     * and so not valid to delete via Dynamic Region Management.
     */
    @Test
    public void executeDestroysLeavesRegionIfMetadataMissing() throws Exception {
        createRegion(regionName);
        Region<String, PdxInstance> metadataRegion = MetadataRegion.getMetadataRegion();
        metadataRegion.destroy(regionName);

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(regionName));
        new DestroyRegion().execute(context);

        verify(resultSender).lastResult(false);
    }

    @Test
    public void executeDestroyWithNullArgs() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("One argument required in list");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(null);
        new DestroyRegion().execute(context);
    }
    
    @Test
    public void executeDestroyWithWrongTypeArgs() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("One argument required in list");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(new String("hello"));
        new DestroyRegion().execute(context);
    }
    
    @Test
    public void executeDestroyWithWrongEmbeddedTypeArgs() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Region name must be non-empty String");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(Integer.MAX_VALUE));
        new DestroyRegion().execute(context);
    }
    
    @Test
    public void executeDestroyWithNullEmbeddedTypeArgs() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Region name must be non-empty String");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList((Object)null));
        new DestroyRegion().execute(context);
    }
    
    @Test
    public void executeDestroyWithInvalidEmbeddedTypeArgs() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Region name must be non-empty String");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(""));
        new DestroyRegion().execute(context);
    }
    
    @Test
    public void executeDestroyWithEmptyListArgs() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("One argument required in list");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(new ArrayList<String>());
        new DestroyRegion().execute(context);
    }

    @Test
    public void executeDestroyWithOverfullListArgs() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("One argument required in list");

    	List<String> args = new ArrayList<>();
    	args.add("one");
    	args.add("two");
    	
        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(args);
        new DestroyRegion().execute(context);
    }
    
    @Test
    public void executeDestroyFailsForSubRegions() {
    	String targetRegionName = regionName + Region.SEPARATOR_CHAR + regionName;

    	expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Region name '" + targetRegionName + "' cannot include reserved char '/'");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(targetRegionName));
        new DestroyRegion().execute(context);
    }
    
    private void createRegion(String name) {
        Region<String, PdxInstance> metadataRegion = MetadataRegion.getMetadataRegion();
        PdxInstance regionOptions = JSONFormatter.fromJSON("{ \"client\": { \"type\": \"CACHING_PROXY\" } , \"server\": {} }");
        metadataRegion.put(name, regionOptions);
        // region is created by the CacheListener
        assertThat(cache.getRegion(name), notNullValue());
        assertThat(metadataRegion.containsKey(name), equalTo(true));
    }
}
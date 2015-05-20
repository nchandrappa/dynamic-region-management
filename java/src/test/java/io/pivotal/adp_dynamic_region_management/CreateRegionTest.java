package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.ResultSender;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
public class CreateRegionTest {
	private static PdxInstance validRegionOptions = null;

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
        validRegionOptions = JSONFormatter.fromJSON("{" +
        		"\"client\": { \"type\": \"CACHING_PROXY\" }" +
        		"," +
        		"\"server\": { \"type\": \"PARTITION\" }" +
        		" }");
    }

    @Test
    public void getId() throws Exception {
        assertThat(new CreateRegion().getId(), equalTo("CreateRegion"));
    }

    @Test
    public void hasResult() throws Exception {
        assertThat(new CreateRegion().hasResult(), equalTo(true));
    }

    @Test
    public void executeSendsTrueResultOnSuccess() throws Exception {
        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(regionName, validRegionOptions));

        new CreateRegion().execute(context);

        verify(resultSender).lastResult(true);
    }

    @Test
    public void executeSendsFalseResultWhenOnlyRegionMetadataAlreadyExists() throws Exception {
        createRegion(regionName);
        cache.getRegion(regionName).destroyRegion();

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(regionName, validRegionOptions));

        new CreateRegion().execute(context);

        verify(resultSender).lastResult(false);
    }

    @Test
    public void executeSendsFalseResultWhenActualRegionAlreadyExist() throws Exception {
    	RegionFactory<?,?> regionFactory = cache.createRegionFactory(RegionShortcut.REPLICATE);
    	regionFactory.create(regionName);

    	when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(regionName, validRegionOptions));

        new CreateRegion().execute(context);

        verify(resultSender).lastResult(false);
    }

    @Test
    public void executeCreateMakesRegionAndMetadata() throws Exception {
        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(regionName, validRegionOptions));

        new CreateRegion().execute(context);

        Region<String, PdxInstance> metadataRegion = MetadataRegion.getMetadataRegion();
        assertThat(cache.getRegion(regionName), notNullValue());
        assertThat(metadataRegion.containsKey(regionName), equalTo(true));
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
    public void executeCreateWithNullArgs() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Two arguments required in list");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(null);
        new CreateRegion().execute(context);
    }
    
    @Test
    public void executeCreateWithWrongTypeArgs() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Two arguments required in list");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(new String("hello"));
        new CreateRegion().execute(context);
    }
    
    @Test
    public void executeCreateWithWrongEmbeddedTypeArgsFirst() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Region name must be non-empty String");

	    PdxInstance regionOptions = JSONFormatter.fromJSON("{}");
	    
        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(Integer.MAX_VALUE, regionOptions));
        new CreateRegion().execute(context);
    }
    
    @Test
    public void executeCreateWithWrongEmbeddedTypeArgsSecond() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Region name 'hello', value should be PdxInstance not java.lang.Integer");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList("hello", Integer.MIN_VALUE));
        new CreateRegion().execute(context);
    }
    
    @Test
    public void executeCreateWithNullEmbeddedTypeArgsFirst() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Region name must be non-empty String");

	    PdxInstance regionOptions = JSONFormatter.fromJSON("{}");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList((Object)null, regionOptions));
        new CreateRegion().execute(context);
    }
    
    @Test
    public void executeCreateWithNullEmbeddedTypeArgsSecond() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Region name 'hello', value cannot be null");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList("hello", (Object)null));
        new CreateRegion().execute(context);
    }
    
    @Test
    public void executeCreateWithInvalidEmbeddedTypeArgs() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Region name must be non-empty String");

	    PdxInstance regionOptions = JSONFormatter.fromJSON("{}");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList("", regionOptions));
        new CreateRegion().execute(context);
    }
    
    @Test
    public void executeCreateWithEmptyListArgs() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Two arguments required in list");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(new ArrayList<String>());
        new CreateRegion().execute(context);
    }

    @Test
    public void executeCreateWithOverfullListArgs() {
		expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Two arguments required in list");

    	List<String> args = new ArrayList<>();
    	args.add("one");
    	args.add("two");
    	args.add("three");
    	
        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(args);
        new CreateRegion().execute(context);
    }
    
    @Test
    public void executeCreateFailsForSubRegions() {
    	String targetRegionName = regionName + Region.SEPARATOR_CHAR + regionName;

    	expectedException.expect(RuntimeException.class);
	    expectedException.expectMessage("Region name '" + targetRegionName + "' cannot include reserved char '/'");

	    PdxInstance regionOptions = JSONFormatter.fromJSON("{}");

        when(context.getResultSender()).thenReturn(resultSender);
        when(context.getArguments()).thenReturn(Arrays.asList(targetRegionName, regionOptions));
        new CreateRegion().execute(context);
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
package io.pivotal.adp_dynamic_region_management;

import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.CacheLoaderException;
import com.gemstone.gemfire.cache.TimeoutException;
import com.gemstone.gemfire.pdx.PdxInstance;

/**
 *<P>Tests that use a mock Gemfire cache to be able to throw rare exceptions on
 *demand.</P>
 *<P>See {@link io.pivotal.adp_dynamic_region_management.MetadataRegionCacheListenerTest}
 *for normal tests using a real cache</P
 */
public class MetadataRegionCacheListenerMockTest {

	private static Objenesis objenesis;
	private static ObjectInstantiator<MetadataRegionCacheListener> metadataRegionCacheListenerInstantiator;
	
	private boolean previousClient;
	
	@Mock
    public Cache cache;
    @Mock
    public EntryEvent<String, PdxInstance> entryEvent;
	@Mock
	public LogWriter logWriter;
	@Mock
	public Region<Object,Object> region;

    @Rule
    public TestName name = new TestName();
	
	private MetadataRegionCacheListener metadataRegionCacheListener = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
    	objenesis = new ObjenesisStd();
    	metadataRegionCacheListenerInstantiator = objenesis.getInstantiatorOf(MetadataRegionCacheListener.class);
	}
	
	@Before
	public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        /* Need a non-mock metadataRegionCacheListener, but cannot use the constructor
         * as that throws an exception from CacheFactory.getAnyInstance()
         */
    	this.metadataRegionCacheListener = metadataRegionCacheListenerInstantiator.newInstance();

        Field cacheField = MetadataRegionCacheListener.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        cacheField.set(this.metadataRegionCacheListener, cache);

        Field logWriterField = MetadataRegionCacheListener.class.getDeclaredField("logWriter");
        logWriterField.setAccessible(true);
        logWriterField.set(this.metadataRegionCacheListener, logWriter);

        // Save original value of static field to restore after test
        this.previousClient = MetadataRegionCacheListener.isClient();
	}

	@After
	public void tearDown() throws Exception {
		Mockito.verifyNoMoreInteractions(this.cache, this.entryEvent, this.logWriter, this.region);

		// Restore original value of static field so not affected by test
		MetadataRegionCacheListener.setClient(this.previousClient);
	}

	@Test
	public void clientAlreadyDestroyed() throws Exception {
		MetadataRegionCacheListener.setClient(true);
		
		String eventKey = this.getCurrentTestName();
		
		String message = "Unable to delete region '" + eventKey + "', because it does not exist";
		
		Mockito.when(this.entryEvent.getKey()).thenReturn(eventKey);
		Mockito.when(this.cache.getRegion(eventKey)).thenReturn(null);

		this.metadataRegionCacheListener.afterDestroy(this.entryEvent);
		
		Mockito.verify(this.entryEvent, Mockito.times(1)).getKey();
		Mockito.verify(this.cache, Mockito.times(1)).getRegion(eventKey);
		Mockito.verify(this.logWriter, Mockito.times(1)).error(message);
	}

	@Test
	public void clientDestroyTimeoutException() throws Exception {
		MetadataRegionCacheListener.setClient(true);
		
		String eventKey = this.getCurrentTestName();
		
		String message = "MetadataRegionCacheListener deleting region named: " + eventKey;
		
		Mockito.when(this.entryEvent.getKey()).thenReturn(eventKey);
		Mockito.when(this.cache.getRegion(eventKey)).thenReturn(this.region);
		Mockito.doThrow(new TimeoutException()).when(this.region).localDestroyRegion();

		try {
			this.metadataRegionCacheListener.afterDestroy(this.entryEvent);
		} catch (TimeoutException expectedToBeThrown) {
		}
		
		Mockito.verify(this.entryEvent, Mockito.times(1)).getKey();
		Mockito.verify(this.cache, Mockito.times(1)).getRegion(eventKey);
		Mockito.verify(this.logWriter, Mockito.times(1)).info(message);
		Mockito.verify(this.region, Mockito.times(1)).localDestroyRegion();
	}

	@Test
	public void clientDestroyOk() throws Exception {
		MetadataRegionCacheListener.setClient(true);
		
		String eventKey = this.getCurrentTestName();
		
		String message = "MetadataRegionCacheListener deleting region named: " + eventKey;
		
		Mockito.when(this.entryEvent.getKey()).thenReturn(eventKey);
		Mockito.when(this.cache.getRegion(eventKey)).thenReturn(this.region);

		this.metadataRegionCacheListener.afterDestroy(this.entryEvent);
		
		Mockito.verify(this.entryEvent, Mockito.times(1)).getKey();
		Mockito.verify(this.cache, Mockito.times(1)).getRegion(eventKey);
		Mockito.verify(this.logWriter, Mockito.times(1)).info(message);
		Mockito.verify(this.region, Mockito.times(1)).localDestroyRegion();
	}

	@Test
	public void serverAlreadyDestroyed() throws Exception {
		MetadataRegionCacheListener.setClient(false);
		
		String eventKey = this.getCurrentTestName();
		
		String message = "Distributed Region.destroyRegion() failed on this node for region '" + eventKey + "', because it does not exist";
		
		Mockito.when(this.entryEvent.getKey()).thenReturn(eventKey);
		Mockito.when(this.cache.getRegion(eventKey)).thenReturn(null);
		Mockito.when(this.logWriter.fineEnabled()).thenReturn(true);
		this.metadataRegionCacheListener.afterDestroy(this.entryEvent);
		
		Mockito.verify(this.entryEvent, Mockito.times(1)).getKey();
		Mockito.verify(this.cache, Mockito.times(1)).getRegion(eventKey);
		Mockito.verify(this.logWriter, Mockito.times(1)).fineEnabled();
		Mockito.verify(this.logWriter, Mockito.times(1)).fine(message);
	}

	@Test
	public void serverDestroyTimeoutException() throws Exception {
		MetadataRegionCacheListener.setClient(false);
		
		String eventKey = this.getCurrentTestName();
		
		String message1 = "MetadataRegionCacheListener deleting region named: " + eventKey;
		String message2 = "Distributed Region.destroyRegion() failed on this node for region '" + eventKey + "'";

		TimeoutException timeoutException = new TimeoutException();
		
		Mockito.when(this.entryEvent.getKey()).thenReturn(eventKey);
		Mockito.when(this.cache.getRegion(eventKey)).thenReturn(this.region);
		Mockito.doThrow(timeoutException).when(this.region).destroyRegion();
		Mockito.when(this.logWriter.errorEnabled()).thenReturn(true);

		this.metadataRegionCacheListener.afterDestroy(this.entryEvent);
		
		Mockito.verify(this.entryEvent, Mockito.times(1)).getKey();
		Mockito.verify(this.cache, Mockito.times(1)).getRegion(eventKey);
		Mockito.verify(this.logWriter).info(message1);
		Mockito.verify(this.region, Mockito.times(1)).destroyRegion();
		Mockito.verify(this.logWriter).errorEnabled();
		Mockito.verify(this.logWriter).error(message2,timeoutException);
	}

	@Test
	public void serverDestroyOtherException() throws Exception {
		MetadataRegionCacheListener.setClient(false);
		
		String eventKey = this.getCurrentTestName();
		
		String message1 = "MetadataRegionCacheListener deleting region named: " + eventKey;
		String message2 = "Distributed Region.destroyRegion() failed on this node for region '" + eventKey + "'";

		CacheLoaderException cacheLoaderException = new CacheLoaderException();

		Mockito.when(this.entryEvent.getKey()).thenReturn(eventKey);
		Mockito.when(this.cache.getRegion(eventKey)).thenReturn(this.region);
		Mockito.doThrow(cacheLoaderException).when(this.region).destroyRegion();
		Mockito.when(this.logWriter.fineEnabled()).thenReturn(true);

		this.metadataRegionCacheListener.afterDestroy(this.entryEvent);
		
		Mockito.verify(this.entryEvent, Mockito.times(1)).getKey();
		Mockito.verify(this.cache, Mockito.times(1)).getRegion(eventKey);
		Mockito.verify(this.logWriter).info(message1);
		Mockito.verify(this.region, Mockito.times(1)).destroyRegion();
		Mockito.verify(this.logWriter).fineEnabled();
		Mockito.verify(this.logWriter).fine(message2,cacheLoaderException);
	}

	@Test
	public void serverDestroyOk() throws Exception {
		MetadataRegionCacheListener.setClient(false);
		
		String eventKey = this.getCurrentTestName();
		
		String message = "MetadataRegionCacheListener deleting region named: " + eventKey;
		
		Mockito.when(this.entryEvent.getKey()).thenReturn(eventKey);
		Mockito.when(this.cache.getRegion(eventKey)).thenReturn(this.region);

		this.metadataRegionCacheListener.afterDestroy(this.entryEvent);
		
		Mockito.verify(this.entryEvent, Mockito.times(1)).getKey();
		Mockito.verify(this.cache, Mockito.times(1)).getRegion(eventKey);
		Mockito.verify(this.logWriter, Mockito.times(1)).info(message);
		Mockito.verify(this.region, Mockito.times(1)).destroyRegion();
	}

    private String getCurrentTestName() {
        return getClass().getSimpleName() + name.getMethodName();
    }
}
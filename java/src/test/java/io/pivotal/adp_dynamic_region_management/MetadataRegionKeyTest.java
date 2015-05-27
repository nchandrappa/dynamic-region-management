package io.pivotal.adp_dynamic_region_management;

import static org.junit.Assert.*;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MetadataRegionKeyTest {

	/**
	 * <P>This class saves and restores the static field <I>cache</I> in 
	 * {@link io.pivotal.adp_dynamic_region_management.MetadataRegion}
	 * across unit tests.
	 * <P>
	 * <P>The need for this is because this module has some tests that use Gemfire to inject a real cache
	 * in the static field and has other tests that use Mockito that inject a fake cache into the field.
	 * However, the {@link io.pivotal.adp_dynamic_region_management.MetadataRegion} class doesn't have
	 * a method to change the <I>cache</I> field that is private to it once set.
	 * </P>
	 * <P>
	 * The solution for now is test methods that use Mockito save and restore the cache field, as this
	 * will contain a Gemfire cache. It's done this way as there are less Mockito tests than true
	 * Gemfire tests.
	 * </P>
	 * <P>
	 *TODO Refactor the tests into separate modules to address this. A test using a Gemfire cache
	 *might not be viewed as a true unit test, and could go into an integration test build module.
	 *</P>
	 */
    public Cache previousCache;

	@Mock
    public Cache cache;
	@Mock
	public LogWriter logWriter;
	@Mock
	public Region<Object,Object> region;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // Mockito doesn't set static fields, and PowerMock is more trouble than value
        Field cacheField = MetadataRegion.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        this.previousCache = (Cache)cacheField.get(null);
        cacheField.set(null, cache);
    }

	@After
	public void tearDown() throws Exception {
		Mockito.verifyNoMoreInteractions(this.cache,this.logWriter,this.region);

        Field cacheField = MetadataRegion.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        cacheField.set(null, this.previousCache);
	}

	@Test
	public void cleanRegionNameEmptyString() {
		String input="";
		String expected = "";

		String actual = MetadataRegion.cleanRegionName(input);
		assertEquals(expected,actual);
	}

	@Test
	public void cleanRegionNameWithWhitespace() {
		String input=" Hello World ";
		String expected = "HelloWorld";

		String actual = MetadataRegion.cleanRegionName(input);
		assertEquals(expected,actual);
	}

	@Test
	public void cleanRegionNameWithOnlyIllegalChars() {
		String input="!@£$%^&*()"; 
		String expected = "";
		
		String actual = MetadataRegion.cleanRegionName(input);
		assertEquals(expected,actual);
	}

	@Test
	public void cleanRegionNameWithMixOfLegalAndIllegalChars() {
		String input="h" + "\u00e8" + "\u0141" + "\u0141" + "\u00d6" + "WORLD"; 
		String expected = "hWORLD";
		
		String actual = MetadataRegion.cleanRegionName(input);
		assertEquals(expected,actual);
	}

	@Test
	public void cleanRegionNameWithoutIllegalChars() {
		String input="123hello123world123";
		String expected = input;
		
		String actual = MetadataRegion.cleanRegionName(input);
		assertEquals(expected,actual);
	}

	@Test
	public void validateRegionNameNull() throws Exception {
	    expectedException.expectMessage("Region name must be non-empty String");
		
		Object key = null;

		MetadataRegion.validateRegionName(key);
	}

	@Test
	public void validateRegionNameClass() throws Exception {
	    expectedException.expectMessage("Region name must be non-empty String");
		
		Integer key = Integer.MAX_VALUE;

		MetadataRegion.validateRegionName(key);
	}

	@Test
	public void validateRegionNameTooSmall() throws Exception {
	    expectedException.expectMessage("Region name must be non-empty String");
		
		String key = "";

		MetadataRegion.validateRegionName(key);
	}

	@Test
	public void validateRegionNameTooBig() throws Exception {
		
	    StringBuffer stringBuffer = new StringBuffer();
	    for(int i=0 ; i<10 ; i++) {
	    	stringBuffer.append("abcdefghijklmnopqrstuvwxyz");
	    }
		String key = stringBuffer.toString();
		
		String warningMessage = "Region name '" + key + "' long, at 260 chars";

		Mockito.when(this.cache.getLogger()).thenReturn(this.logWriter);
		
		MetadataRegion.validateRegionName(key);

		Mockito.verify(this.cache, Mockito.times(1)).getLogger();
		Mockito.verify(this.logWriter, Mockito.times(1)).warning(warningMessage);
	}

	@Test
	public void validateRegionNameSubRegion() throws Exception {
	    expectedException.expectMessage("Region name 'numerator/denominator' cannot include reserved char '/'");
		
		String key = "numerator/denominator";

		MetadataRegion.validateRegionName(key);
	}

	@Test
	public void validateRegionNameReserved() throws Exception {
	    expectedException.expectMessage("Region name '__regionAttributesMetadata' cannot begin '__', reserved for Gemfire");
		
		String key = MetadataRegion.REGION_ATTRIBUTES_METADATA_REGION;

		MetadataRegion.validateRegionName(key);
	}

	@Test
	public void validateRegionNameSpecialChars() throws Exception {
		String key = "$dollar";

		String warningMessage = "Region name '$dollar' contains special characters";

		Mockito.when(this.cache.getLogger()).thenReturn(this.logWriter);
		
		MetadataRegion.validateRegionName(key);

		Mockito.verify(this.cache, Mockito.times(1)).getLogger();
		Mockito.verify(this.logWriter, Mockito.times(1)).warning(warningMessage);
	}

	@Test
	public void validateRegionNameLeadingDigits() throws Exception {
		String key = "123abc";

		String warningMessage = "Region name '123abc' should begin with a letter";

		Mockito.when(this.cache.getLogger()).thenReturn(this.logWriter);
		
		MetadataRegion.validateRegionName(key);

		Mockito.verify(this.cache, Mockito.times(1)).getLogger();
		Mockito.verify(this.logWriter, Mockito.times(1)).warning(warningMessage);
	}
	
	@Test
	public void validateRegionNameExactMatchExists() throws Exception {
		String key = "two";

		Object[] otherRegionNames = { "one", "two", "three" };
		Set<Object> keySet = new HashSet<>(Arrays.asList(otherRegionNames));
		
		Mockito.when(this.cache.getRegion(MetadataRegion.REGION_ATTRIBUTES_METADATA_REGION)).thenReturn(this.region);
		Mockito.when(this.region.keySet()).thenReturn(keySet);
		Mockito.when(this.cache.getLogger()).thenReturn(this.logWriter);
		
		MetadataRegion.validateRegionName(key);

		Mockito.verify(this.cache, Mockito.times(1)).getRegion(MetadataRegion.REGION_ATTRIBUTES_METADATA_REGION);
		Mockito.verify(this.region, Mockito.times(1)).keySet();
	}

	@Test
	public void validateRegionNameSimilarExistsCaseInsensitive() throws Exception {
		String key = "TWO";

		String warningMessage = "Region name 'TWO' similar to existing region 'two'";

		Object[] otherRegionNames = { "one", "two", "three" };
		Set<Object> keySet = new HashSet<>(Arrays.asList(otherRegionNames));
		
		Mockito.when(this.cache.getRegion(MetadataRegion.REGION_ATTRIBUTES_METADATA_REGION)).thenReturn(this.region);
		Mockito.when(this.region.keySet()).thenReturn(keySet);
		Mockito.when(this.cache.getLogger()).thenReturn(this.logWriter);
		
		MetadataRegion.validateRegionName(key);

		Mockito.verify(this.cache, Mockito.times(1)).getRegion(MetadataRegion.REGION_ATTRIBUTES_METADATA_REGION);
		Mockito.verify(this.region, Mockito.times(1)).keySet();
		Mockito.verify(this.cache, Mockito.times(1)).getLogger();
		Mockito.verify(this.logWriter, Mockito.times(1)).warning(warningMessage);
	}

	@Test
	public void validateRegionNameSimilarExistsSpecialChars() throws Exception {
		String key = "TWO";

		String warningMessage = "Region name 'TWO' similar to existing region 't!@@@w!o@&^%&^%&^@%!'";

		Object[] otherRegionNames = { "one", "t!@@@w!o@&^%&^%&^@%!", "three!" };
		Set<Object> keySet = new HashSet<>(Arrays.asList(otherRegionNames));
		
		Mockito.when(this.cache.getRegion(MetadataRegion.REGION_ATTRIBUTES_METADATA_REGION)).thenReturn(this.region);
		Mockito.when(this.region.keySet()).thenReturn(keySet);
		Mockito.when(this.cache.getLogger()).thenReturn(this.logWriter);
		
		MetadataRegion.validateRegionName(key);

		Mockito.verify(this.cache, Mockito.times(1)).getRegion(MetadataRegion.REGION_ATTRIBUTES_METADATA_REGION);
		Mockito.verify(this.region, Mockito.times(1)).keySet();
		Mockito.verify(this.cache, Mockito.times(1)).getLogger();
		Mockito.verify(this.logWriter, Mockito.times(1)).warning(warningMessage);
	}

	@Test
	public void validateRegionNameDissimilarExistsSpecialChars() throws Exception {
		String key = "A";

		Object[] otherRegionNames = { "one", "two", "three", "$$b$$" };
		Set<Object> keySet = new HashSet<>(Arrays.asList(otherRegionNames));
		
		Mockito.when(this.cache.getRegion(MetadataRegion.REGION_ATTRIBUTES_METADATA_REGION)).thenReturn(this.region);
		Mockito.when(this.region.keySet()).thenReturn(keySet);
		
		MetadataRegion.validateRegionName(key);

		Mockito.verify(this.cache, Mockito.times(1)).getRegion(MetadataRegion.REGION_ATTRIBUTES_METADATA_REGION);
		Mockito.verify(this.region, Mockito.times(1)).keySet();
	}

	@Test
	public void validateRegionNameSimilarExistsGood() throws Exception {
		String key = "A";

		Object[] otherRegionNames = { "b", "C", "d", "e" };
		Set<Object> keySet = new HashSet<>(Arrays.asList(otherRegionNames));
		
		Mockito.when(this.cache.getRegion(MetadataRegion.REGION_ATTRIBUTES_METADATA_REGION)).thenReturn(this.region);
		Mockito.when(this.region.keySet()).thenReturn(keySet);
		
		MetadataRegion.validateRegionName(key);

		Mockito.verify(this.cache, Mockito.times(1)).getRegion(MetadataRegion.REGION_ATTRIBUTES_METADATA_REGION);
		Mockito.verify(this.region, Mockito.times(1)).keySet();
	}

}

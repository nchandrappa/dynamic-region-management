package io.pivotal.adp_dynamic_region_management;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;

public class MetadataRegionValueTest {

	@Rule
    public TestName name = new TestName();

    public String regionName;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
        regionName = getClass().getSimpleName() + name.getMethodName();
    }
	
	@Test
	public void validateRegionValueNull() throws Exception {
	    expectedException.expectMessage("Region name 'MetadataRegionValueTestvalidateRegionValueNull', value cannot be null");
		
		Object regionOptions = null;

		MetadataRegion.validateRegionOptions(regionName, regionOptions);
	}

	@Test
	public void validateRegionValueNonPdxInteger() throws Exception {
	    expectedException.expectMessage("Region name 'MetadataRegionValueTestvalidateRegionValueNonPdxInteger', value should be PdxInstance not java.lang.Integer");
		
	    // Completely the wrong type
	    Integer regionOptions = Integer.MAX_VALUE;

		MetadataRegion.validateRegionOptions(regionName, regionOptions);
	}
	
	@Test
	public void validateRegionValueNonPdxString() throws Exception {
	    expectedException.expectMessage("Region name 'MetadataRegionValueTestvalidateRegionValueNonPdxString', value should be PdxInstance not java.lang.String");
		
	    // Could be coalesced to PDXInstance, but isn't.
	    String regionOptions = "";

		MetadataRegion.validateRegionOptions(regionName, regionOptions);
	}
	
	/*TODO Do we want this really to fail ? The code could detect it is being passed a String, and try{}catch with
	 * JSONFormatter to see if it can make the PdxInstance it needs from the content given.
	 */
	@Test
	public void validateRegionValueNonPdxJSON() throws Exception {
	    expectedException.expectMessage("Region name 'MetadataRegionValueTestvalidateRegionValueNonPdxJSON', value should be PdxInstance not java.lang.String");
		
	    // This is a **String**, that contains JSON but hasn't been converted to a PdxInstance
		String regionOptions = "{" +
        		"\"client\": { \"type\": \"CACHING_PROXY\" }" +
        		"," +
        		"\"server\": { \"type\": \"PARTITION\" }" +
        		" }";

		MetadataRegion.validateRegionOptions(regionName, regionOptions);
	}

	@Test
	public void validateRegionValuePdxJSONMissingServer() throws Exception {
	    expectedException.expectMessage("Region name 'MetadataRegionValueTestvalidateRegionValuePdxJSONMissingServer', value must specify 'server' option");
        PdxInstance regionOptions = JSONFormatter.fromJSON("{ " +
        		" \"client\" : { \"type\": \"CACHING_PROXY\" }"  +
        		"}");

		MetadataRegion.validateRegionOptions(regionName, regionOptions);
	}

	@Test
	public void validateRegionValuePdxEmptyServerSection() throws Exception {
		
        PdxInstance regionOptions = JSONFormatter.fromJSON("{ " +
        		" \"client\" : { \"type\": \"CACHING_PROXY\" }"  +
        		"," +
        		" \"server\" : { }"  +
        		"}");

		MetadataRegion.validateRegionOptions(regionName, regionOptions);
	}

	@Test
	public void validateRegionValuePdxNonEmptyServerSection() throws Exception {

        PdxInstance regionOptions = JSONFormatter.fromJSON("{ " +
        		" \"client\" : { \"type\": \"CACHING_PROXY\" }"  +
        		"," +
        		" \"server\" : { \"type\": \"PARTITION\" }"  +
        		"}");

	    MetadataRegion.validateRegionOptions(regionName, regionOptions);
	}
}

package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.pdx.PdxInstance;

public class MetadataRegion {
    public static final String REGION_ATTRIBUTES_METADATA_REGION = "__regionAttributesMetadata";

    public static final int    REGION_COUNT_WARNING_LEVEL = 1000;

    /* Subregions are not yet supported.
     * 
     * Space and full stop are allowed, means OQL needs escape with single quote.
     */
    public static final char[] RESERVED_CHARS = { Region.SEPARATOR_CHAR };
    
    public static String getName(){
    	return REGION_ATTRIBUTES_METADATA_REGION;
    }
    
    public static Region<String,PdxInstance> getMetadataRegion() {
        Cache cache = CacheFactory.getAnyInstance();

        Region<String, PdxInstance> metaRegion = cache.getRegion(REGION_ATTRIBUTES_METADATA_REGION);

        return metaRegion;
    }

    /* Admin overhead of region stats, etc, may outweigh benefits as more and more
     * regions are added.
     */
    public static void checkLimits() {
    	try {
    		Region<?,?> metadataRegion = getMetadataRegion();
    		int size = metadataRegion.size();
    		if(size >= REGION_COUNT_WARNING_LEVEL && size%50==0) {
    			LogWriter logWriter = CacheFactory.getAnyInstance().getLogger();
    			logWriter.warning(metadataRegion.getFullPath() + " has " + size + " entries, possible performance overhead");
    		}
    	} catch (Exception e) {
    		;
    	}
    }

    public static void validateRegionName(Object regionName) throws Exception {

    	if(regionName==null || !(regionName instanceof String) || ((String)regionName).length()==0) {
    		throw new Exception("Region name must be non-empty String");
    	}
    	
    	for(char c : RESERVED_CHARS) {
    		if(((String)regionName).indexOf(c)>=0) {
    			throw new Exception("Region name '" + regionName + "' cannot include reserved char '" + c + "'");
    		}
    	}
    }

	public static void validateRegionOptions(String regionName, Object regionOptions) throws Exception {
	    if(regionOptions==null) {
	    	throw new Exception("Region name '" + regionName + "' options cannot be null");
	    }
	    if(!(regionOptions instanceof PdxInstance)) {
	    	throw new Exception("Region name '" + regionName + "' options should be PdxInstance not " + regionOptions.getClass().getCanonicalName());
	    } 
	}
    
}

package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.pdx.PdxInstance;

public class MetadataRegion {
    public static final String REGION_ATTRIBUTES_METADATA_REGION = "__regionAttributesMetadata";

    /* Subregions are not yet supported.
     * "." is used by OQL.
     */
    public static final char[] RESERVED_CHARS = { '.' , Region.SEPARATOR_CHAR };

    public static String getName(){
    	return REGION_ATTRIBUTES_METADATA_REGION;
    }
    
    public static Region<String,PdxInstance> getMetadataRegion() {
        Cache cache = CacheFactory.getAnyInstance();

        Region<String, PdxInstance> metaRegion = cache.getRegion(REGION_ATTRIBUTES_METADATA_REGION);

        return metaRegion;
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
}

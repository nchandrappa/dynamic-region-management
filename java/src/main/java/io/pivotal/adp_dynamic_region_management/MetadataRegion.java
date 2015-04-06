package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.pdx.PdxInstance;

public class MetadataRegion {
    public static final String REGION_ATTRIBUTES_METADATA_REGION = "__regionAttributesMetadata";

    public static String getName(){
    	return REGION_ATTRIBUTES_METADATA_REGION;
    }
    
    public static Region<String,PdxInstance> getMetadataRegion() {
        Cache cache = CacheFactory.getAnyInstance();

        Region<String, PdxInstance> metaRegion = cache.getRegion(REGION_ATTRIBUTES_METADATA_REGION);

        return metaRegion;
    }
}

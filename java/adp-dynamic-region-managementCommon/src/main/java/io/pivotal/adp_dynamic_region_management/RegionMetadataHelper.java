package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.pdx.PdxInstance;
import com.gemstone.gemfire.pdx.PdxInstanceFactory;

public class RegionMetadataHelper {
    public static <T> void updateRegionMetadata(String regionName, String fieldName, T cloningEnabled) {
        Region<String, PdxInstance> metadataRegion = MetadataRegion.getMetadataRegion();

        PdxInstance regionOptionsMetadata = metadataRegion.get(regionName);

        PdxInstance updatedServerOptionsMetadata;

        if (regionOptionsMetadata.hasField("server")) {
            PdxInstance serverOptionsMetadata = (PdxInstance) regionOptionsMetadata.getField("server");
            updatedServerOptionsMetadata = PdxInstanceHelper.setValue(serverOptionsMetadata, fieldName, cloningEnabled);
        } else {
            PdxInstanceFactory pdxInstanceFactory = CacheFactory.getAnyInstance().createPdxInstanceFactory("");
            pdxInstanceFactory.writeObject(fieldName, cloningEnabled);
            updatedServerOptionsMetadata = pdxInstanceFactory.create();
        }

        PdxInstance updatedRegionOptionsMetadata = PdxInstanceHelper.setValue(regionOptionsMetadata, "server", updatedServerOptionsMetadata);
        metadataRegion.put(regionName, updatedRegionOptionsMetadata);
    }
}

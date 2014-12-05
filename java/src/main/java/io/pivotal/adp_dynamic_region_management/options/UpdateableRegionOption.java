package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.Region;

public interface UpdateableRegionOption {

    public void updateMetadataRegion(String regionName);

    public void updateRegion(Region region);

}

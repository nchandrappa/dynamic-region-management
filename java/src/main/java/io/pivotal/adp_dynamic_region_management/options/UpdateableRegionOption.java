package io.pivotal.adp_dynamic_region_management.options;

import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

import com.gemstone.gemfire.cache.Region;

public interface UpdateableRegionOption {

    public void updateMetadataRegion(String regionName) throws RegionOptionsInvalidException;

    @SuppressWarnings("rawtypes")
	public void updateRegion(Region region) throws RegionOptionsInvalidException;

}

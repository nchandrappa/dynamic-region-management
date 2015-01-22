package io.pivotal.adp_dynamic_region_management;

import java.util.Properties;

import com.gemstone.gemfire.cache.RegionFactory;

public interface DistributionPolicy {
	public void apply(RegionFactory factory, String policyName) throws RegionOptionsInvalidException;
	public void init(Properties props);
}

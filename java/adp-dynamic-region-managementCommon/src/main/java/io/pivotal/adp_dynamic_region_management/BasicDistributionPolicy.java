package io.pivotal.adp_dynamic_region_management;

import java.util.Properties;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.RegionFactory;


public class BasicDistributionPolicy implements DistributionPolicy {

	private String []regionalSenders;
	private String []globalSenders;
	private String defaultPolicy;
	
	@Override
	public void apply(RegionFactory factory, String policyName) throws RegionOptionsInvalidException {
		if (policyName != null){
			if (validPolicyName(policyName) != true)
				throw new RegionOptionsInvalidException("distributionPolicy region option has invalid value");
		}
		
		if (policyName == null) policyName = defaultPolicy;
		
		if (policyName.equals("REGIONAL")){
			for(String sender : regionalSenders) factory.addGatewaySenderId(sender);
		} else if (policyName.equals("GLOBAL")){
			for(String sender : globalSenders) factory.addGatewaySenderId(sender);
		}
		
	}

	/**
	 * Requires the following properties
	 * 
	 * REGIONAL: value is a comma separated list of pre-existing gateway sender ids
	 *           to which regions with a REGIONAL distribution policy will forward events
	 * GLOBAL: value is a comma separated list of pre-existing gateway sender ids
	 *           to which regions with a GLOBAL distribution policy will forward events
	 * DEFAULT: value must be LOCAL, REGIONAL or GLOBAL.  Regions that do not specify
	 * 			a distribution policy will inherit this setting
	 * 
	 * Regions with a LOCAL distribution policy will not forward events to an gateway 
	 * 
	 */
	@Override
	public void init(Properties props) {
		String regionalProp = requiredProp(props, "REGIONAL_DISTRIBUTION_POLICY");
		String globalProp = requiredProp(props, "GLOBAL_DISTRIBUTION_POLICY");
		
		this.regionalSenders = regionalProp.split(",");
		this.globalSenders = globalProp.split(",");

		this.defaultPolicy = requiredProp(props, "DEFAULT_DISTRIBUTION_POLICY");
		if (!validPolicyName(this.defaultPolicy))
			error("invalid default policy: " + this.defaultPolicy + " valid values are \"GLOBAL\",\"REGIONAL\",\"LOCAL\"");
	}
	
	private String requiredProp(Properties props, String name){
		String val = props.getProperty(name);
		if (val == null){
			error("required property missing in BasicDistributionPolicy.initialize: " + name);
		}
		
		return val;
	}
	
	private boolean validPolicyName(String policyName){		
		if (policyName.equals("LOCAL"))
			return true;
		else if (policyName.equals("REGIONAL"))
			return true;
		else if (policyName.equals("GLOBAL"))
			return true;
		else
			return false;
	}
	
	// always throws a RuntimeException
	private void error(String msg){
		LogWriter log = CacheFactory.getAnyInstance().getLogger();
		log.error(msg);
		throw new RuntimeException(msg);
	}

}

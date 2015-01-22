package io.pivotal.adp_dynamic_region_management.options;

import io.pivotal.adp_dynamic_region_management.DistributionPolicy;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

import java.util.List;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;

public class GatewaySenderIdsOption extends RegionOption<List> {

	DistributionPolicy distributionPolicy;

	public GatewaySenderIdsOption(PdxInstance serverOptions,
			DistributionPolicy distributionPolicy) {
		super(serverOptions);
		this.distributionPolicy = distributionPolicy;
	}

	/**
	 * if distributionPolicy is null, the gateway-sender-ids region attribute
	 * will be set directly from the gatewaySenderIds region options, otherwise,
	 * these will be completely ignored and the gateway-sender-ids will be
	 * controlled by the distribution policy
	 */
	public void setOptionOnRegionFactory(RegionFactory regionFactory)
			throws RegionOptionsInvalidException {
		if (distributionPolicy == null) {
			List vals = value();
			if (vals != null){
				for (Object gatewaySenderId : value()) {
					String gatewaySenderIdString = (String) gatewaySenderId;
					try {
						regionFactory.addGatewaySenderId(gatewaySenderIdString);
					} catch (IllegalStateException e) {
						throw new RegionOptionsInvalidException(String.format(
								"Unable to set gateway sender ID %s",
								gatewaySenderIdString), e);
					}
				}
			}
		} else {
			if (serverOptions.hasField(distributionPolicyFieldName())){
				warn("passed " + getFieldName() + " option will be ignored while creating region because the distribution policy takes precendence");
			}
			
			String distributionPolicyName = null;
			if (serverOptions.hasField(distributionPolicyFieldName())) distributionPolicyName = (String)  serverOptions.getField(distributionPolicyFieldName());
			
			distributionPolicy.apply(regionFactory, distributionPolicyName);
		}
	}

	private String distributionPolicyFieldName(){
		return "distributionPolic";
	}
	
	protected String getFieldName() {
		return "gatewaySenderIds";
	}

	// this returns true because we always need to apply the distribution policy
	@Override
	public boolean isAnOption() {
		return true;
	}

	private void warn(String msg){
		LogWriter log = CacheFactory.getAnyInstance().getLogger();
		log.warning(msg);
	}

	@Override
	protected List value() throws RegionOptionsInvalidException {
		return (List) serverOptions.getField(getFieldName());
	}
}

package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

import java.util.List;

public class GatewaySenderIdsOption extends RegionOption<List> {
    public GatewaySenderIdsOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        for (Object gatewaySenderId : value()) {
            String gatewaySenderIdString = (String) gatewaySenderId;
            try {
                regionFactory.addGatewaySenderId(gatewaySenderIdString);
            } catch (IllegalStateException e) {
                throw new RegionOptionsInvalidException(
                        String.format("Unable to set gateway sender ID %s", gatewaySenderIdString), e);
            }
        }
    }

    protected List value() {
        return (List) serverOptions.getField(getFieldName());
    }

    protected String getFieldName() {
        return "gatewaySenderIds";
    }
}

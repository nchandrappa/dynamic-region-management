package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;

public class MulticastEnabledOption extends RegionOption<Boolean> {
    public MulticastEnabledOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    public void setOptionOnRegionFactory(RegionFactory regionFactory) {
        regionFactory.setMulticastEnabled(value());
    }

    protected Boolean value() {
        return (Boolean) serverOptions.getField(getFieldName());
    }

    protected String getFieldName() {
        return "multicastEnabled";
    }
}

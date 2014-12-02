package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;

public class IgnoreJTAOption extends RegionOption<Boolean> {
    public IgnoreJTAOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    public void setOptionOnRegionFactory(RegionFactory regionFactory) {
        regionFactory.setIgnoreJTA(value());
    }

    protected Boolean value() {
        return (Boolean) serverOptions.getField(getFieldName());
    }

    protected String getFieldName() {
        return "ignoreJTA";
    }
}

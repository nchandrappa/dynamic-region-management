package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;

public class EnableAsyncConflationOption extends RegionOption<Boolean> {
    public EnableAsyncConflationOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    public void setOptionOnRegionFactory(RegionFactory regionFactory) {
        regionFactory.setEnableAsyncConflation(value());
    }

    protected Boolean value() {
        return (Boolean) serverOptions.getField(getFieldName());
    }

    protected String getFieldName() {
        return "enableAsyncConflation";
    }
}
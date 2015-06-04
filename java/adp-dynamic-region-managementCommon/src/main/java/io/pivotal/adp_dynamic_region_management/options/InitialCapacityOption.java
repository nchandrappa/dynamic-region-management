package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;

public class InitialCapacityOption extends RegionOption<Integer> {
    public InitialCapacityOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    public void setOptionOnRegionFactory(RegionFactory regionFactory) {
        regionFactory.setInitialCapacity(value());
    }

    protected Integer value() {
        return ((Number) serverOptions.getField(getFieldName())).intValue();
    }

    protected String getFieldName() {
        return "initialCapacity";
    }
}

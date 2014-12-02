package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;

public class LoadFactorOption extends RegionOption<Float> {
    public LoadFactorOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    public void setOptionOnRegionFactory(RegionFactory regionFactory) {
        regionFactory.setLoadFactor(value());
    }

    protected Float value() {
        return ((Number) serverOptions.getField(getFieldName())).floatValue();
    }

    protected String getFieldName() {
        return "loadFactor";
    }
}

package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

public abstract class RegionOption<T> {
    protected final PdxInstance serverOptions;

    protected RegionOption(PdxInstance serverOptions) {
        this.serverOptions = serverOptions;
    }

    public boolean isAnOption() {
        return serverOptions.hasField(getFieldName());
    }

    public abstract void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException;

    protected abstract T value() throws RegionOptionsInvalidException;

    protected abstract String getFieldName();
}

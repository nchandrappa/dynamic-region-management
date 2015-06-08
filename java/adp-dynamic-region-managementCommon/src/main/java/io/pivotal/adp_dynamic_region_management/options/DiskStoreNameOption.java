package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;

public class DiskStoreNameOption extends RegionOption<String> {
    public DiskStoreNameOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    public void setOptionOnRegionFactory(RegionFactory regionFactory) {
        regionFactory.setDiskStoreName(value());
    }

    protected String value() {
        return (String) serverOptions.getField(getFieldName());
    }

    protected String getFieldName() {
        return "diskStoreName";
    }
}

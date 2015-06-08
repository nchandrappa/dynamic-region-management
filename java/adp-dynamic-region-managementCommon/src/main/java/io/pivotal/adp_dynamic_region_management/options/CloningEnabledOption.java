package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.RegionMetadataHelper;

public class CloningEnabledOption extends RegionOption<Boolean> implements UpdateableRegionOption {
    public CloningEnabledOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    public void setOptionOnRegionFactory(RegionFactory regionFactory) {
        regionFactory.setCloningEnabled(value());
    }

    protected Boolean value() {
    	Boolean field = (Boolean) serverOptions.getField(getFieldName());
    	return (field!=null ? field : false);
    }

    protected String getFieldName() {
        return "cloningEnabled";
    }

    @Override
    public void updateMetadataRegion(String regionName) {
        RegionMetadataHelper.updateRegionMetadata(regionName, getFieldName(), value());
    }

    @Override
    public void updateRegion(Region region) {
        region.getAttributesMutator().setCloningEnabled(value());
    }
}

package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.ExpirationAttributes;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;
import com.gemstone.gemfire.pdx.PdxInstanceFactory;
import io.pivotal.adp_dynamic_region_management.ExpirationAttributesFactory;
import io.pivotal.adp_dynamic_region_management.RegionMetadataHelper;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;


public class EntryIdleTimeOption extends RegionOption<ExpirationAttributes> implements UpdateableRegionOption {
    public EntryIdleTimeOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    @Override
    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        ExpirationAttributes expirationAttributes = value();
        if(expirationAttributes != null) {
            regionFactory.setEntryIdleTimeout(expirationAttributes);
        }
    }

    @Override
    protected ExpirationAttributes value() throws RegionOptionsInvalidException {
        PdxInstance pdxInstance = (PdxInstance) serverOptions.getField(getFieldName());
        return new ExpirationAttributesFactory(pdxInstance).create();
    }

    @Override
    protected String getFieldName() {
        return "entryIdleTime";
    }

    @Override
    public void updateMetadataRegion(String regionName) throws RegionOptionsInvalidException {
        ExpirationAttributes expirationAttributes = value();

        PdxInstanceFactory pdxInstanceFactory = CacheFactory.getAnyInstance().createPdxInstanceFactory("");
        pdxInstanceFactory.writeObject("action", expirationAttributes.getAction().toString());
        pdxInstanceFactory.writeObject("timeout", expirationAttributes.getTimeout());

        PdxInstance pdxInstance = pdxInstanceFactory.create();

        RegionMetadataHelper.updateRegionMetadata(regionName, getFieldName(), pdxInstance);
    }

    @Override
    public void updateRegion(Region region) throws RegionOptionsInvalidException {
        region.getAttributesMutator().setEntryIdleTimeout(value());
    }
}

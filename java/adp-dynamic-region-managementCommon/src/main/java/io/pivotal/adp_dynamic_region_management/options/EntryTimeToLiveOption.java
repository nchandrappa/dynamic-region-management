package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.ExpirationAttributes;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.ExpirationAttributesFactory;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

public class EntryTimeToLiveOption extends RegionOption<ExpirationAttributes> {
    public EntryTimeToLiveOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    @Override
    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        ExpirationAttributes expirationAttributes = value();
        if(expirationAttributes != null) {
            regionFactory.setEntryTimeToLive(expirationAttributes);
        }
    }

    @Override
    protected ExpirationAttributes value() throws RegionOptionsInvalidException {
        PdxInstance pdxInstance = (PdxInstance) serverOptions.getField(getFieldName());
        return new ExpirationAttributesFactory(pdxInstance).create();
    }

    @Override
    protected String getFieldName() {
        return "entryTimeToLive";
    }
}

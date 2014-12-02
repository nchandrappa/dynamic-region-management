package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

public class IndexUpdateTypeOption extends RegionOption<String> {
    public IndexUpdateTypeOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        if (value().equals("synchronous")) {
            regionFactory.setIndexMaintenanceSynchronous(true);
        } else if (value().equals("asynchronous")) {
            regionFactory.setIndexMaintenanceSynchronous(false);
        } else {
            throw new RegionOptionsInvalidException(
                    String.format("Invalid indexUpdateType `%s`, valid values are `synchronous` or `asynchronous`.",
                            value()));
        }
    }

    protected String value() {
        return (String) serverOptions.getField(getFieldName());
    }

    protected String getFieldName() {
        return "indexUpdateType";
    }
}

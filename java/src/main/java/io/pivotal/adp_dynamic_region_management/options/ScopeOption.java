package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.Scope;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

public class ScopeOption extends RegionOption<String> {
    public ScopeOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        try {
            regionFactory.setScope(Scope.fromString(value()));
        } catch (IllegalArgumentException e) {
            throw new RegionOptionsInvalidException(
                    String.format("Invalid scope: `%s`.", value()), e);
        }
    }

    protected String value() {
        return (String) serverOptions.getField(getFieldName());
    }

    protected String getFieldName() {
        return "scope";
    }
}

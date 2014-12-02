package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

public class KeyConstraintOption extends RegionOption<Class> {
    public KeyConstraintOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    @Override
    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        regionFactory.setKeyConstraint(value());
    }

    @Override
    protected Class value() throws RegionOptionsInvalidException {
        String className = (String) serverOptions.getField(getFieldName());
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RegionOptionsInvalidException(
                    String.format("Could not find class: `%s`.", className), e);
        }
    }

    @Override
    protected String getFieldName() {
        return "keyConstraint";
    }
}

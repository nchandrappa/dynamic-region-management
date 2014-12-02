package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

public class DataPolicyOption extends RegionOption<DataPolicy> {
    public DataPolicyOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    public void setOptionOnRegionFactory(RegionFactory regionFactory)  throws RegionOptionsInvalidException {
        regionFactory.setDataPolicy(value());
    }

    protected DataPolicy value() throws RegionOptionsInvalidException {
        String dataPolicy = (String) serverOptions.getField("dataPolicy");
        if (dataPolicy != null) {
            byte index = 0;
            while (com.gemstone.gemfire.cache.DataPolicy.fromOrdinal(index) != null) {
                com.gemstone.gemfire.cache.DataPolicy possibleDataPolicy = com.gemstone.gemfire.cache.DataPolicy.fromOrdinal(index);
                if (possibleDataPolicy.toString().equals(dataPolicy)) {
                    return possibleDataPolicy;
                }
                index++;
            }
            throw new RegionOptionsInvalidException(String.format("Invalid dataPolicy: `%s`.", dataPolicy));
        }
        return null;
    }

    protected String getFieldName() {
        return "dataPolicy";
    }
}

package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.pdx.PdxInstance;

public class RegionOptionsValidator {
    private PdxInstance regionOptions;

    RegionOptionsValidator(PdxInstance regionOptions) {
        this.regionOptions = regionOptions;
    }

    public boolean validate() throws RegionOptionsInvalidException {
        PdxInstance clientOptions = (PdxInstance) regionOptions.getField("client");

        if(clientOptions == null) {
            throw new RegionOptionsInvalidException("Invalid region options. Expected client to be defined.");
        }

        String clientType = (String) clientOptions.getField("type");

        if(clientType == null) {
            throw new RegionOptionsInvalidException("Invalid region options. Expected client.type to be defined.");
        }

        PdxInstance serverOptions = (PdxInstance) regionOptions.getField("server");
        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions, null);
        regionOptionsFactory.validate();

        return true;
    }
}

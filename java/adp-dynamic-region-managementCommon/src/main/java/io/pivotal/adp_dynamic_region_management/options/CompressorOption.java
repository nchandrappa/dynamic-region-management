package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.compression.Compressor;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.Instantiator;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

public class CompressorOption extends RegionOption<Compressor> {
    public CompressorOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    @Override
    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        regionFactory.setCompressor(value());
    }

    @Override
    protected Compressor value() throws RegionOptionsInvalidException {
        String className = (String) serverOptions.getField(getFieldName());
        return new Instantiator<Compressor>(className).instantiate();
    }

    @Override
    protected String getFieldName() {
        return "compressor";
    }
}

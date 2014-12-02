package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.CacheLoader;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.Instantiator;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

public class CacheLoaderOption extends RegionOption<CacheLoader> {
    public CacheLoaderOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    @Override
    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        regionFactory.setCacheLoader(value());
    }

    @Override
    protected CacheLoader value() throws RegionOptionsInvalidException {
        Instantiator<CacheLoader> instantiator = new Instantiator<CacheLoader>((String) serverOptions.getField(getFieldName()));
        CacheLoader cacheLoader = instantiator.instantiate();
        return cacheLoader;
    }

    @Override
    protected String getFieldName() {
        return "cacheLoader";
    }

}

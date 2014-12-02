package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.Instantiator;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

import java.util.ArrayList;
import java.util.List;

public class CacheListenerOption extends RegionOption<List<CacheListener>> {
    public CacheListenerOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    @Override
    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        for(CacheListener cacheListener : value()) {
            regionFactory.addCacheListener(cacheListener);
        }
    }

    @Override
    protected List<CacheListener> value() throws RegionOptionsInvalidException {
        List<String> classNames = (List<String>) serverOptions.getField(getFieldName());
        List<CacheListener> cacheListeners = new ArrayList<CacheListener>();

        for(String className : classNames) {
            Instantiator<CacheListener> instantiator = new Instantiator<CacheListener>(className);
            cacheListeners.add(instantiator.instantiate());
        }

        return cacheListeners;
    }

    @Override
    protected String getFieldName() {
        return "cacheListener";
    }
}

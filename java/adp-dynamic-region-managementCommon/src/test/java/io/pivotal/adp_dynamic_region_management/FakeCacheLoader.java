package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.CacheLoader;
import com.gemstone.gemfire.cache.CacheLoaderException;
import com.gemstone.gemfire.cache.LoaderHelper;

public class FakeCacheLoader implements CacheLoader {
    @Override
    public Object load(LoaderHelper loaderHelper) throws CacheLoaderException {
        return null;
    }

    @Override
    public void close() {

    }
}

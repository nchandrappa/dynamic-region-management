package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;

public abstract class CacheSingleton {
    static private Cache cache;

    public static Cache getCache() {
        if(cache == null) {
            CacheFactory cacheFactory = new CacheFactory();
            cache = cacheFactory.create();
        }

        return cache;
    }
}

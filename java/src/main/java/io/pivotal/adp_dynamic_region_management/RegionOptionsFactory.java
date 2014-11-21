package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.pdx.PdxInstance;

public class RegionOptionsFactory {
    private Cache cache;
    private PdxInstance serverOptions;

    public RegionOptionsFactory(PdxInstance serverOptions) {
        this.serverOptions = serverOptions;
        this.cache = CacheSingleton.getCache();
    }

    public RegionFactory getRegionFactory() {
        if(serverOptions == null) {
            return this.cache.createRegionFactory();
        }

        RegionShortcut regionShortcut = getRegionShortcut();
        if(regionShortcut != null) {
            return this.cache.createRegionFactory(regionShortcut);
        } else {
            return this.cache.createRegionFactory();
        }
    }

    private RegionShortcut getRegionShortcut() {
        String serverRegionType = (String) serverOptions.getField("type");
        if (serverRegionType == null) {
            return null;
        }
        return RegionShortcut.valueOf(serverRegionType);
    }
}

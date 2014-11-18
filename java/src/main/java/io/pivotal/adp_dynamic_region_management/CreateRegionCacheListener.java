package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.gemstone.gemfire.pdx.PdxInstance;

import java.util.Map;
import java.util.Properties;

public class CreateRegionCacheListener extends CacheListenerAdapter<String,PdxInstance>  implements Declarable {

    private Cache cache;

    public CreateRegionCacheListener() {
        this.cache = CacheFactory.getAnyInstance();
    }

    public void afterCreate(EntryEvent<String,PdxInstance> event) {
        createRegion(event.getKey(), event.getNewValue());
    }

    public void afterRegionCreate(RegionEvent<String,PdxInstance> event) {
        Region<String,PdxInstance> region = event.getRegion();
        for (Map.Entry<String,PdxInstance> entry : region.entrySet()) {
            createRegion(entry.getKey(), entry.getValue());
        }
    }

    private void createRegion(String regionName, PdxInstance pdxInstance) {
        RegionShortcut regionShortcut = getRegionShortcut(pdxInstance);
        RegionFactory regionFactory;

        if(regionShortcut != null) {
            regionFactory = this.cache.createRegionFactory(regionShortcut);
        } else {
            regionFactory = this.cache.createRegionFactory();
        }


        logInfo("CreateRegionCacheListener creating region named: " + regionName);

        try {
            Region region = regionFactory.create(regionName);
            logInfo("CreateRegionCacheListener created: " + region);
        } catch (RegionExistsException e) {
            logInfo("Unable to create region `" + regionName + "`, because it already exists.");
            throw e;
        }
    }

    private RegionShortcut getRegionShortcut(PdxInstance pdxInstance) {
        PdxInstance serverConfig = (PdxInstance) pdxInstance.getField("server");
        if(serverConfig == null) {
            return null;
        }
        String serverRegionType = (String) serverConfig.getField("type");
        if (serverRegionType == null) {
            return null;
        }
        return RegionShortcut.valueOf(serverRegionType);
    }

    private void logInfo(String message) {
        this.cache.getLogger().info(message);
    }

    public void init(Properties properties) {
    }
}

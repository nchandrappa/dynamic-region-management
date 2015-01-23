package io.pivotal.adp_dynamic_region_management;

import io.pivotal.adp_dynamic_region_management.options.CloningEnabledOption;

import java.util.Map;
import java.util.Properties;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionEvent;
import com.gemstone.gemfire.cache.RegionExistsException;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.gemstone.gemfire.pdx.PdxInstance;

public class MetadataRegionCacheListener extends CacheListenerAdapter<String,PdxInstance>  implements Declarable {

    private Cache cache;
    private DistributionPolicy distributionPolicy = null;

    public MetadataRegionCacheListener() {
        this.cache = CacheFactory.getAnyInstance();
    }

    @Override
    public void afterCreate(EntryEvent<String,PdxInstance> event) {
        createRegion(event.getKey(), event.getNewValue());
    }

    @Override
    public void afterRegionCreate(RegionEvent<String,PdxInstance> event) {
        Region<String,PdxInstance> region = event.getRegion();
        for (Map.Entry<String,PdxInstance> entry : region.entrySet()) {
            createRegion(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void afterUpdate(EntryEvent<String, PdxInstance> event) {
    }

    private void createRegion(String regionName, PdxInstance pdxInstance) {
        PdxInstance serverOptions = (PdxInstance) pdxInstance.getField("server");
        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions, distributionPolicy);
        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();

        logInfo("MetadataRegionCacheListener creating region named: " + regionName);

        try {
            Region region = regionFactory.create(regionName);
            logInfo("MetadataRegionCacheListener created: " + region);
        } catch (RegionExistsException e) {
            logInfo("Unable to create region `" + regionName + "`, because it already exists.");
            throw e;
        }
    }

    @Override
    public void afterDestroy(EntryEvent<String, PdxInstance> event) {
        cache.getRegion(event.getKey()).destroyRegion();
    }

    private void logInfo(String message) {
        this.cache.getLogger().info(message);
    }

    public void init(Properties properties) {
    	String className = properties.getProperty("distributionPolicyClass");
    	if (className != null){
    		try {
    			Class clazz = Class.forName(className);
    			this.distributionPolicy = (DistributionPolicy) clazz.newInstance();
    			this.distributionPolicy.init(properties);
    		} catch(ClassNotFoundException x){
    			throw new RuntimeException("distributionPolicyClass was not found: " + className);
    		} catch(InstantiationException | IllegalAccessException xx){
    			throw new RuntimeException("distributionPolicy class was found but instantiation failed: " + className, xx);
    		} 
    	}
    }
}

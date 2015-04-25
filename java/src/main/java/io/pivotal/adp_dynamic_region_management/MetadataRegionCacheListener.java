package io.pivotal.adp_dynamic_region_management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionEvent;
import com.gemstone.gemfire.cache.RegionExistsException;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;

public class MetadataRegionCacheListener extends CacheListenerAdapter<String,PdxInstance>  implements Declarable {

	private static String FAILURE_REDUNDANCY_RECOVERY_DELAY_PARAMETER = "FAILURE_REDUNDANCY_RECOVERY_DELAY";
	private static String STARTUP_REDUNDANCY_RECOVERY_DELAY_PARAMETER = "STARTUP_REDUNDANCY_RECOVERY_DELAY";
	
	private static String PARTITION_ATTRIBUTES_FIELD = "partitionAttributes";
	private static String RECOVERY_DELAY_FIELD = "recoveryDelay";
	private static String STARTUP_RECOVERY_DELAY_FIELD = "startupRecoveryDelay";
	
    private Cache cache;
    private LogWriter logWriter;
    private DistributionPolicy distributionPolicy = null;
    private int redundancyRecoveryDelay = Integer.MIN_VALUE; //indicates not set
    private int startupRedundancyRecoveryDelay = Integer.MIN_VALUE; //indicates not set

    public MetadataRegionCacheListener() {
        this.cache = CacheFactory.getAnyInstance();
        this.logWriter = this.cache.getLogger();
    }

    @Override
    public void afterCreate(EntryEvent<String,PdxInstance> event) {
        createRegion(event.getKey(), event.getNewValue());
    }


    @Override
    public void afterUpdate(EntryEvent<String, PdxInstance> event) {
    }
    
	/*
	* Create region may be called in an event handler or in the cache initializer.
    * It throws exceptions.  If it is called in the cache initializer the exception
    * would abort the startup process, which is the intended behavior.  If it is 
    * called in the CacheListener, the exception will be silently ignored, which
    * is harmless.
    * 
    * package scope since its called from the CacheInitializer
	*/
    void createRegion(String regionName, PdxInstance pdxInstance) {
        PdxInstance serverOptions = (PdxInstance) pdxInstance.getField("server");
        
        // enforce overrides by setting server options here
        if ( (redundancyRecoveryDelay != Integer.MIN_VALUE) || (startupRedundancyRecoveryDelay != Integer.MIN_VALUE) ){
        	try {
	        	String serverOptionsJSON = JSONFormatter.toJSON(serverOptions);
	        	ObjectMapper mapper = new ObjectMapper();
	        	JsonNode root = mapper.readTree(serverOptionsJSON);
	        	ObjectNode partitionAttributes = (ObjectNode) root.get(PARTITION_ATTRIBUTES_FIELD);
	        	if (partitionAttributes == null){
	        		partitionAttributes = mapper.createObjectNode();
	        		((ObjectNode) root).put(PARTITION_ATTRIBUTES_FIELD, partitionAttributes);
	        	}
	        	
	        	if (redundancyRecoveryDelay != Integer.MIN_VALUE)
	        		partitionAttributes.put(RECOVERY_DELAY_FIELD, redundancyRecoveryDelay);
	        	
	        	if (startupRedundancyRecoveryDelay != Integer.MIN_VALUE)
	        		partitionAttributes.put(STARTUP_RECOVERY_DELAY_FIELD, startupRedundancyRecoveryDelay);
	
	        	serverOptionsJSON = mapper.writeValueAsString(root);
	        	logInfo("Server options have been overridden - effective options are now: " + serverOptionsJSON);
	        	serverOptions = JSONFormatter.fromJSON(serverOptionsJSON);
        	} catch ( IOException x) {
        		this.logWriter.severe("error while applying server side overrides to region defintions", x);
        		throw new RuntimeException("error while applying server side overrides to region defintions", x);
        	}
        }
        
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
    	String regionName = event.getKey();
    	Region<?,?> region = cache.getRegion(regionName);
    	if(region!=null) {
            logInfo("MetadataRegionCacheListener deleting region named: " + regionName);
    		region.destroyRegion();
    	} else {
    		if(this.logWriter.warningEnabled()) {
    			this.logWriter.warning("Unable to delete region '" + regionName + "', because it does not exist");
    		}
    	}
    }

    private void logInfo(String message) {
		this.logWriter.info(message);
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
    	
    	// capture properties
    	String s = properties.getProperty(FAILURE_REDUNDANCY_RECOVERY_DELAY_PARAMETER);
    	if (s != null){
    		try {
    			redundancyRecoveryDelay = Integer.parseInt(s);
    		} catch(NumberFormatException x){
    			throw new RuntimeException(FAILURE_REDUNDANCY_RECOVERY_DELAY_PARAMETER + " property must be an integer: " + s);
    		}
    	}
    	
    	s = properties.getProperty(STARTUP_REDUNDANCY_RECOVERY_DELAY_PARAMETER);
    	if (s != null){
    		try {
    			startupRedundancyRecoveryDelay = Integer.parseInt(s);
    		} catch(NumberFormatException x){
    			throw new RuntimeException(STARTUP_REDUNDANCY_RECOVERY_DELAY_PARAMETER + " property must be an integer: " + s);
    		}
    	}
    }
}

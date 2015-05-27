package io.pivotal.adp_dynamic_region_management;

import java.io.IOException;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.CacheWriterException;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionExistsException;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.TimeoutException;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;

import io.pivotal.adp_dynamic_region_management.options.CloningEnabledOption;

public class MetadataRegionCacheListener extends CacheListenerAdapter<String,PdxInstance>  implements Declarable {

	private static String FAILURE_REDUNDANCY_RECOVERY_DELAY_PARAMETER = "FAILURE_REDUNDANCY_RECOVERY_DELAY";
	private static String STARTUP_REDUNDANCY_RECOVERY_DELAY_PARAMETER = "STARTUP_REDUNDANCY_RECOVERY_DELAY";
	
	private static String PARTITION_ATTRIBUTES_FIELD = "partitionAttributes";
	private static String RECOVERY_DELAY_FIELD = "recoveryDelay";
	private static String STARTUP_RECOVERY_DELAY_FIELD = "startupRecoveryDelay";
	
	private static final String DEFAULT_CLIENT_POOL_NAME = "myPool";
	private static final String DEFAULT_CLIENT_REGION_TYPE = "PROXY";
	private static ClientRegionFactory<?,?> proxyRegionFactory = null;

	private static boolean client;
    private Cache cache;
    private LogWriter logWriter;
    private DistributionPolicy distributionPolicy = null;
    private int redundancyRecoveryDelay = Integer.MIN_VALUE; //indicates not set
    private int startupRedundancyRecoveryDelay = Integer.MIN_VALUE; //indicates not set

    public MetadataRegionCacheListener() {
        this.cache = CacheFactory.getAnyInstance();
        try {
        	// This throws an exception if not a client cache.
        	ClientCacheFactory.getAnyInstance();
        	setClient(true);
        } catch (IllegalStateException e) {
        	setClient(false);
        }
        this.logWriter = this.cache.getLogger();
    }

    @Override
    public void afterCreate(EntryEvent<String,PdxInstance> event) {
        createRegion(event.getKey(), event.getNewValue());
    }

    @Override
    public void afterUpdate(EntryEvent<String, PdxInstance> event) {
        PdxInstance regionOptions = event.getNewValue();
        PdxInstance serverOptions = (PdxInstance) regionOptions.getField("server");

        String regionName = event.getKey();
        Region<?,?> region = CacheFactory.getAnyInstance().getRegion(regionName);

        new CloningEnabledOption(serverOptions).updateRegion(region);
    }

    /*  Don't throw exceptions to a listener event method, as they're
     * not passed back to the client that triggered the event. Logging
     * an error will be sufficient.
     */
    public void createRegion(String regionName, PdxInstance pdxInstance) {
    	
    	try {
        	MetadataRegion.validateRegionName(regionName);
        	MetadataRegion.validateRegionOptions(regionName, pdxInstance);
    	} catch (Exception exception) {
    		// An init() method has to catch the exception, although letting it fail would be better
    		CacheFactory.getAnyInstance().getLogger().error("Create region failure for '" + (regionName==null?"NULL":regionName) + "'", exception);
    	}

        if(isClient()) {
        	createRegionOnClient(regionName, pdxInstance);
        } else {
        	createRegionOnServer(regionName, pdxInstance);
        }
    }
    
    private void createRegionOnClient(String regionName, PdxInstance pdxInstance) {
        PdxInstance clientOptions = (PdxInstance) pdxInstance.getField("client");
        String poolName = null;
        String type = null;
        
        if(clientOptions!=null) {
        	poolName = (String) clientOptions.getField("poolName");
        	type = (String) clientOptions.getField("type");
        }

        if(!DEFAULT_CLIENT_POOL_NAME.equals(poolName) &&
           !DEFAULT_CLIENT_REGION_TYPE.equals(type)) {
			throw new RuntimeException("Client region type '" + (type==null?"<omitted>":type) 
								    + "' poolName '" + (type==null?"<omitted>":poolName) 
								    + "' not implemented yet");
		}
        
		if(proxyRegionFactory==null) {
			proxyRegionFactory = ((ClientCache)cache).createClientRegionFactory(ClientRegionShortcut.PROXY);
			proxyRegionFactory.setPoolName(DEFAULT_CLIENT_POOL_NAME);
		}
        
        this.logWriter.fine("MetadataRegionCacheListener creating region named: " + regionName);

        try {
            Region<?,?> region = proxyRegionFactory.create(regionName);
            this.logWriter.fine("MetadataRegionCacheListener created: " + region);
        } catch (RegionExistsException e) {
            logInfo("Unable to create region `" + regionName + "`, because it already exists.");
        }

    }

    private void createRegionOnServer(String regionName, PdxInstance pdxInstance) {
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
        RegionFactory<?,?> regionFactory = regionOptionsFactory.getRegionFactory();

        
        logInfo("MetadataRegionCacheListener creating region named: " + regionName);

        try {
            Region<?,?> region = regionFactory.create(regionName);
            logInfo("MetadataRegionCacheListener created: " + region);
        } catch (RegionExistsException e) {
            logInfo("Unable to create region `" + regionName + "`, because it already exists.");
            throw e;
        }
    }

    public void afterDestroy(EntryEvent<String, PdxInstance> event) {
        destroyRegion(event.getKey());
    }

    public void destroyRegion(final String regionName) {
        if(isClient()) {
        	destroyRegionOnClient(regionName);
        } else {
        	destroyRegionOnServer(regionName);
        }
    }
    
    private void destroyRegionOnClient(final String regionName) {
    	/* Destroy may fail to start or may fail to finish, but for different
    	 * reasons on client and server.
    	 * 
		 * On the client, the region to be deleted should be there to begin
		 * with. It is possible that it would be missing, for instance, if
		 * the region had been deleted manually via the API. If so, log this
		 * as an error, as bypassing the dynamic region management service
		 * to manipulate regions under its control should be discouraged.
		 * 
		 * The destroy once started should complete, and should only fail
		 * if there is an internal error. Do not catch any such errors,
		 * allow this to be visible in the calling chain in the logs.
    	 */
    	Region<?,?> region = cache.getRegion(regionName);
    	if(region!=null) {
            logInfo("MetadataRegionCacheListener deleting region named: " + regionName);
    		region.localDestroyRegion();
    	} else {
    		this.logWriter.error("Unable to delete region '" + regionName + "', because it does not exist");
    	}
    }

    private void destroyRegionOnServer(final String regionName) {
    	/* Destroy may fail to start or may fail to finish, but for different
    	 * reasons on client and server.
    	 * 
    	 * On the server, the destroy uses Region.destroyRegion() which is
    	 * a distributed operation. So it's very likely that one of the
    	 * other servers may have deleted the region just at the very
    	 * instant this method starts. So if the region is missing it's
    	 * very trivial, so give a minor log message.
		 * 
		 * Once the destroy starts, it may collide with another started
		 * for the same region on a different server at the exact same
		 * time, or it may fail due to errors in the API. Try to separate
		 * these out in terms of which get minor log messages and which
		 * are worth a higher log level message.
		 */
     	Region<?,?> region = cache.getRegion(regionName);
    	if(region!=null) {
            logInfo("MetadataRegionCacheListener deleting region named: " + regionName);
            try {
            	region.destroyRegion();
            } catch (CacheWriterException|TimeoutException exception1) {
            	if(this.logWriter.errorEnabled()) {
            		this.logWriter.error("Distributed Region.destroyRegion() failed on this node for region '" + regionName + "'", exception1);
            	}
            } catch (Exception exception2) {
            	if(this.logWriter.fineEnabled()) {
            		this.logWriter.fine("Distributed Region.destroyRegion() failed on this node for region '" + regionName + "'", exception2);
            	}
            }
    	} else {
    		if(this.logWriter.fineEnabled()) {
    			this.logWriter.fine("Distributed Region.destroyRegion() failed on this node for region '" + regionName + "', because it does not exist");
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
    			Class<?> clazz = Class.forName(className);
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

	public static boolean isClient() {
		return client;
	}

	public static void setClient(boolean client) {
		MetadataRegionCacheListener.client = client;
	}

}

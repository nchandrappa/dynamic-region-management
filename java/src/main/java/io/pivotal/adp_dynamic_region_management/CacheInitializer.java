package io.pivotal.adp_dynamic_region_management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.server.CacheServer;
import com.gemstone.gemfire.cache.wan.GatewayReceiver;
import com.gemstone.gemfire.cache.wan.GatewaySender;
import com.gemstone.gemfire.pdx.PdxInstance;

public class CacheInitializer implements Declarable {

	@Override
	public void init(Properties props) {
		//TODO - better error checking
		int cacheServerPort = Integer.parseInt(props.getProperty("cacheServerPort"));
		int gatewayReceiverStartPort = Integer.parseInt(System.getProperty("GATEWAY_RECEIVER_START_PORT"));
		int gatewayReceiverEndPort = Integer.parseInt(System.getProperty("GATEWAY_RECEIVER_END_PORT"));
		
		Cache cache = CacheFactory.getAnyInstance();
		LogWriter log = cache.getLogger();
		//TODO don't hard code this
		Region<String,PdxInstance> region = cache.getRegion("__regionAttributesMetadata");
		CacheListener<String,PdxInstance> cacheListeners[] = region.getAttributes().getCacheListeners();
		//TODO - in general, need to refactor so create region does not have to live in the cache listener
		MetadataRegionCacheListener cl = (MetadataRegionCacheListener) cacheListeners[0];

        ArrayList<String> keys = new ArrayList<String>(500);
        keys.addAll(region.keySet());
        Collections.sort(keys);
        for (String regionName : keys) {
        	try {
        		/* Validate the metadata. 
        		 * It should only be created via this API so always be correct, but it's not impossible
        		 * that manual attempts are made via GFSH, or that the format will change and the old
        		 * contents will be invalid
        		 */
                MetadataRegion.validateRegionName(regionName);
                MetadataRegion.validateRegionOptions(regionName, region.get(regionName));
                cl.createRegion(regionName, region.get(regionName));
        	} catch (Exception exception) {
        		// An init() method has to catch the exception, although letting it fail would be better
        		log.error("Create region failure for '" + (regionName==null?"NULL":regionName) + "'", exception);
        	}
        }
        
        StartupConductorThread startupConductor 
        	= new StartupConductorThread(cacheServerPort, gatewayReceiverStartPort, gatewayReceiverEndPort);

        startupConductor.start();
	}

//	public static class GatewaySenderStarterThread extends Thread {
//
//		private GatewaySender sender;
//
//		public GatewaySenderStarterThread(GatewaySender sender) {
//			this.sender = sender;
//		}
//
//		@Override
//		public void run() {
//			// TODO - what do whe do if this fails
//			sender.start();
//		}
//
//	}
//
	public static class StartupConductorThread extends Thread {

		private int cacheServerPort;
		private int receiverStartPort;
		private int receiverEndPort;

		public StartupConductorThread(int cacheServerPort,
				int receiverStartPort, int receiverEndPort) {
			this.cacheServerPort = cacheServerPort;
			this.receiverEndPort = receiverEndPort;
			this.receiverStartPort = receiverStartPort;
		}

		@Override
		public void run() {
			Cache cache = CacheFactory.getAnyInstance();
			LogWriter log = cache.getLogger();
			try {
				// start the senders
				log.info("StartupConductor: starting gateway senders");
				for (GatewaySender sender : cache.getGatewaySenders()) {
					sender.start();
				}

				// start the cache servers
				// TODO - need the ability to set the bind address
				log.info("StartupConductor: starting cache server on port " + Integer.valueOf(cacheServerPort));
				CacheServer cs = cache.addCacheServer();
				cs.setPort(cacheServerPort);
				cs.start();

				log.info("StartupConductor: starting gateway receivers in port range " + Integer.valueOf(receiverStartPort) + "-" +  Integer.valueOf(receiverEndPort));
				// create and start the receivers
				GatewayReceiver receiver = cache.createGatewayReceiverFactory()
						.setStartPort(receiverStartPort)
						.setEndPort(receiverEndPort).create();

				receiver.start();
				log.info("StartupConductor: startup sequence is complete");

			} catch (Exception x) {
				log.severe("StartupConductor: error during startup senquence - cache will shut down", x);
				cache.close();
			}
		}

	}

}

package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import java.nio.file.*;

public abstract class CacheSingleton {
    static private Cache cache;

    public static Cache getCache() {
    	
        if(cache == null) {
        	CacheSingleton.setSystemPropertiesForJunit();
        	
            CacheFactory cacheFactory = new CacheFactory();
            cacheFactory.set("cache-xml-file", "src/test/resources/test.xml");
            cacheFactory.setPdxPersistent(true);
            cache = cacheFactory.create();
        }

        return cache;
    }
    
    /* So that can run Junit from Maven build inside or outside of Vagrant VM.
     */
    public static void setSystemPropertiesForJunit() {    	
    	Path directory = Paths.get("/vagrant/tmp");
    	if(Files.isDirectory(directory)) {
           	System.setProperty("JAVA_TEST_DISK_STORE", "/vagrant/tmp/javaTestDiskStore");
    	} else {
           	System.setProperty("JAVA_TEST_DISK_STORE", "target" + System.getProperty("file.separator") + "javaTestDiskStore");
    	}    	
    }
    
}

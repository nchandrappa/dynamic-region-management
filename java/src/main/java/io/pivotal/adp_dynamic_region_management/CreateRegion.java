package io.pivotal.adp_dynamic_region_management;

// based on http://gemfire.docs.pivotal.io/8.0.0/userguide/developing/region_options/dynamic_region_creation.html

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.pdx.PdxInstance;

import java.util.List;
import java.util.Properties;

import static io.pivotal.adp_dynamic_region_management.ExceptionHelpers.sendStrippedException;

public class CreateRegion implements Function, Declarable {
	private static final long serialVersionUID = 1L;

    private final Cache cache;

    public CreateRegion() {
        this.cache = CacheFactory.getAnyInstance();
    }

    public void execute(FunctionContext context) {
        try {
        	Object arguments = context.getArguments();
            if(arguments==null || !(arguments instanceof List) || ((List<?>)arguments).size()!=2) {
            	throw new Exception("Two arguments required in list");
            } 

            Object regionName = ((List<?>) arguments).get(0);
            MetadataRegion.validateRegionName(regionName);

            Object regionOptions = ((List<?>) arguments).get(1);
            if(regionOptions==null || !(regionOptions instanceof PdxInstance)) {
            	throw new Exception("Second argument should be PdxInstance");
            } 

            boolean status = createOrRetrieveRegion((String)regionName, (PdxInstance)regionOptions);
            context.getResultSender().lastResult(status);
        } catch (Exception exception) {
            sendStrippedException(context, exception);
        }
    }

    private boolean createOrRetrieveRegion(String regionName, PdxInstance regionOptions) throws RuntimeException, RegionOptionsInvalidException {
        Region<?,?> region = this.cache.getRegion(regionName);
        PdxInstance previousMetadata = MetadataRegion.getMetadataRegion().get(regionName);
        
        if ((region != null) || (previousMetadata!=null)) { return false; }

        new RegionOptionsValidator(regionOptions).validate();
       
        MetadataRegion.getMetadataRegion().put(regionName, regionOptions);
        MetadataRegion.checkLimits();

        // the MetadataRegionCacheListener should fire synchronously for the previous put

        region = this.cache.getRegion(regionName);
        if (region == null) {
            throw new RuntimeException("Region was not created for some reason.");
        }

        return true;
    }

    public String getId() {
        return getClass().getSimpleName();
    }

    public boolean optimizeForWrite() {
        return false;
    }

    public boolean isHA() {
        return true;
    }

    public boolean hasResult() {
        return true;
    }

    public void init(Properties properties) {
    	
    }
}

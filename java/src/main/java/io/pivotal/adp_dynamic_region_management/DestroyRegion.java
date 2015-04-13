package io.pivotal.adp_dynamic_region_management;

import static io.pivotal.adp_dynamic_region_management.ExceptionHelpers.sendStrippedException;

import java.util.List;
import java.util.Properties;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryNotFoundException;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;

public class DestroyRegion implements Function, Declarable {
	private static final long serialVersionUID = 1L;

	public DestroyRegion() {
    }

    @Override
    public void init(Properties properties) {
    }

    @Override
    public boolean hasResult() {
        return true;
    }

	@Override
    public void execute(FunctionContext context) {
        try {
        	Object arguments = context.getArguments();
            if(arguments==null || !(arguments instanceof List) || ((List<?>)arguments).size()!=1) {
            	throw new Exception("One argument required in list");
            } 

            Object regionName = ((List<?>) arguments).get(0);
            MetadataRegion.validateRegionName(regionName);
            
            boolean result = destroyRegion((String)regionName);
            context.getResultSender().lastResult(result);
        } catch (Exception exception) {
            sendStrippedException(context, exception);
        }
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean optimizeForWrite() {
        return false;
    }

    @Override
    public boolean isHA() {
        return false;
    }

    private boolean destroyRegion(String regionName) {
        try {
        	MetadataRegion.getMetadataRegion().destroy(regionName);
            // MetadataRegionCacheListener destroys the region in its afterDestroy
            return true;
        } catch (EntryNotFoundException exception) {
            return false;
        }
    }
}

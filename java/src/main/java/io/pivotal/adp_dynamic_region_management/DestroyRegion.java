package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryNotFoundException;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.pdx.PdxInstance;

import java.util.List;
import java.util.Properties;

import static io.pivotal.adp_dynamic_region_management.ExceptionHelpers.sendStrippedException;

public class DestroyRegion implements Function, Declarable {
    private Region<String,PdxInstance> regionAttributesMetadataRegion;

    public DestroyRegion() {
        this.regionAttributesMetadataRegion = MetadataRegion.getMetadataRegion();
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
            List arguments = (List) context.getArguments();
            String regionName = (String) arguments.get(0);
            boolean result = destroyRegion(regionName);
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
            this.regionAttributesMetadataRegion.destroy(regionName);
            // MetadataRegionCacheListener destroys the region in its afterDestroy
            return true;
        } catch (EntryNotFoundException exception) {
            return false;
        }
    }
}

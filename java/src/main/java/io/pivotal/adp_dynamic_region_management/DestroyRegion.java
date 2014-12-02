package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.pdx.PdxInstance;

import java.util.List;
import java.util.Properties;

public class DestroyRegion implements Function, Declarable {
    private final Cache cache;

    protected final Region<String,PdxInstance> regionAttributesMetadataRegion;

    public DestroyRegion() {
        this.cache = CacheFactory.getAnyInstance();
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
            Region region = cache.getRegion(regionName);
            boolean result = destroyRegion(region);
            context.getResultSender().lastResult(result);
        } catch (Exception exception) {
            context.getResultSender().sendException(exception);
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

    private boolean destroyRegion(Region region) {
        if (region == null) {
            return false;
        }
        region.destroyRegion();
        this.regionAttributesMetadataRegion.destroy(region.getName());
        return true;
    }
}
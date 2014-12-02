package io.pivotal.adp_dynamic_region_management;

// based on http://gemfire.docs.pivotal.io/8.0.0/userguide/developing/region_options/dynamic_region_creation.html

import com.gemstone.gemfire.cache.*;

import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.pdx.PdxInstance;

import java.util.List;
import java.util.Properties;

public class CreateRegion implements Function, Declarable {

    private final Cache cache;

    protected final Region<String,PdxInstance> regionAttributesMetadataRegion;

    public CreateRegion() {
        this.cache = CacheFactory.getAnyInstance();
        this.regionAttributesMetadataRegion = MetadataRegion.getMetadataRegion();
    }

    public void execute(FunctionContext context) {
        try {
            List arguments = (List) context.getArguments();
            String regionName = (String) arguments.get(0);
            PdxInstance regionOptions = (PdxInstance) arguments.get(1);

            boolean status = createOrRetrieveRegion(regionName, regionOptions);
            context.getResultSender().lastResult(status);
        } catch (Exception exception) {
            context.getResultSender().sendException(exception);
        }
    }

    private boolean createOrRetrieveRegion(String regionName, PdxInstance regionOptions) throws RuntimeException, RegionOptionsInvalidException {
        Region region = this.cache.getRegion(regionName);

        if (region != null) { return false; }

        new RegionOptionsValidator(regionOptions).validate();

        this.regionAttributesMetadataRegion.put(regionName, regionOptions);

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

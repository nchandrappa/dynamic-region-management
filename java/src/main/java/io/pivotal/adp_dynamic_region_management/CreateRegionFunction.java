package io.pivotal.adp_dynamic_region_management;

// based on http://gemfire.docs.pivotal.io/8.0.0/userguide/developing/region_options/dynamic_region_creation.html

import com.gemstone.gemfire.cache.*;

import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.pdx.PdxInstance;

import java.util.Properties;

public class CreateRegionFunction implements Function, Declarable {

    private final Cache cache;

    private final Region<String,PdxInstance> regionAttributesMetadataRegion;

    private static final String REGION_ATTRIBUTES_METADATA_REGION = "__regionAttributesMetadata";

    public CreateRegionFunction() {
        this.cache = CacheFactory.getAnyInstance();
        this.regionAttributesMetadataRegion = createRegionAttributesMetadataRegion();
    }

    public void execute(FunctionContext context) {
        try {
            Object[] arguments = (Object[]) context.getArguments();
            String regionName = (String) arguments[0];
            PdxInstance regionAttributesPdx = (PdxInstance) arguments[1];

            // Create or retrieve region
            boolean status = createOrRetrieveRegion(regionName, regionAttributesPdx);

            // Return status
            context.getResultSender().lastResult(status);
        } catch (Exception exception) {
            context.getResultSender().sendException(exception);
        }
    }

    private boolean createOrRetrieveRegion(String regionName, PdxInstance pdxInstance) throws RuntimeException {
        Region region = this.cache.getRegion(regionName);

        if (region != null) { return false; }

        // Put the attributes into the metadata region. The afterCreate call will actually create the region.
        this.regionAttributesMetadataRegion.put(regionName, pdxInstance);

        // Retrieve the region after creating it
        region = this.cache.getRegion(regionName);
        if (region == null) {
            throw new RuntimeException("Region was not created for some reason.");
        }

        return true;
    }

    private Region<String,PdxInstance> createRegionAttributesMetadataRegion() {
        Region<String, PdxInstance> metaRegion = this.cache.getRegion(REGION_ATTRIBUTES_METADATA_REGION);
        if (metaRegion == null) {
            RegionFactory<String, PdxInstance> factory = this.cache.createRegionFactory();
            factory.setDataPolicy(DataPolicy.REPLICATE);
            factory.setScope(Scope.DISTRIBUTED_ACK);
            factory.addCacheListener(new CreateRegionCacheListener());
            metaRegion = factory.create(REGION_ATTRIBUTES_METADATA_REGION);
        }
        return metaRegion;
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

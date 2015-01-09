package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.options.CloningEnabledOption;

import java.util.List;
import java.util.Properties;

import static io.pivotal.adp_dynamic_region_management.ExceptionHelpers.sendStrippedException;

public class UpdateRegion implements Function, Declarable {
    private Cache cache;

    public UpdateRegion() {
        this.cache = CacheFactory.getAnyInstance();
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
            PdxInstance regionOptions = (PdxInstance) arguments.get(1);
            boolean result = updateRegion(regionName, regionOptions);
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

    private boolean updateRegion(String regionName, PdxInstance regionOptions) throws RegionOptionsInvalidException {
        Region region = this.cache.getRegion(regionName);

        if (region != null) {
            PdxInstance serverOptions = (PdxInstance) regionOptions.getField("server");
            new CloningEnabledOption(serverOptions).updateMetadataRegion(regionName);

            return true;
        }

        return false;
    }

}

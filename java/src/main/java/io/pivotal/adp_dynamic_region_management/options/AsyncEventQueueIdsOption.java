package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;

import java.util.List;

public class AsyncEventQueueIdsOption extends RegionOption<List> {
    public AsyncEventQueueIdsOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    public void setOptionOnRegionFactory(RegionFactory regionFactory) {
        for (Object asyncEventQueueId : value()) {
            String asyncEventQueueIdString = (String) asyncEventQueueId;
            regionFactory.addAsyncEventQueueId(asyncEventQueueIdString);
        }
    }

    protected List value() {
        return (List) serverOptions.getField(getFieldName());
    }

    protected String getFieldName() {
        return "asyncEventQueueIds";
    }
}

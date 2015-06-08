package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.EvictionAction;
import com.gemstone.gemfire.cache.EvictionAttributes;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.util.ObjectSizer;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

public class EvictionAttributesOption extends RegionOption<EvictionAttributes> {
    public EvictionAttributesOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    @Override
    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        regionFactory.setEvictionAttributes(value());
    }

    @Override
    protected EvictionAttributes value() throws RegionOptionsInvalidException {
        PdxInstance evictionAttributes = (PdxInstance) serverOptions.getField(getFieldName());

        if (evictionAttributes.getFieldNames().size() > 1) {
            throw new RegionOptionsInvalidException(
                    String.format("You must specify only one algorithm for evictionAttributes: %s", evictionAttributes.getFieldNames().toString()));
        }


        PdxInstance lruEntryCount = (PdxInstance) evictionAttributes.getField("lruEntryCount");
        if(lruEntryCount != null) {
            return getLruEntryCountEvictionAttributes(lruEntryCount);
        }

        PdxInstance lruHeapPercentage = (PdxInstance) evictionAttributes.getField("lruHeapPercentage");
        if(lruHeapPercentage != null) {
            return getLruHeapPercentageEvictionAttributes(lruHeapPercentage);
        }

        PdxInstance lruMemorySize = (PdxInstance) evictionAttributes.getField("lruMemorySize");
        if(lruMemorySize != null) {
            return getLruMemorySizeEvictionAttributes(lruMemorySize);
        }

        if (!evictionAttributes.getFieldNames().isEmpty()) {
            throw new RegionOptionsInvalidException(
                    String.format("Invalid evictionAttributes algorithm: %s", evictionAttributes.getFieldNames().toString()));
        }

        return null;
    }

    private EvictionAttributes getLruMemorySizeEvictionAttributes(PdxInstance lruMemorySize) {
        Number maximumEntries = (Number) lruMemorySize.getField("maximum");
        String actionName = (String) lruMemorySize.getField("action");

        if (actionName == null) {
            if (maximumEntries == null) {
                return EvictionAttributes.createLRUMemoryAttributes();
            }
            return EvictionAttributes.createLRUMemoryAttributes(maximumEntries.intValue());
        }
        if (maximumEntries == null) {
            return EvictionAttributes.createLRUMemoryAttributes(ObjectSizer.DEFAULT, EvictionAction.parseAction(actionName));
        }
        return EvictionAttributes.createLRUMemoryAttributes(maximumEntries.intValue(), ObjectSizer.DEFAULT, EvictionAction.parseAction(actionName));
    }

    private EvictionAttributes getLruHeapPercentageEvictionAttributes(PdxInstance lruHeapPercentage) {
        String actionName = (String) lruHeapPercentage.getField("action");

        if (actionName == null) {
            return EvictionAttributes.createLRUHeapAttributes(ObjectSizer.DEFAULT);
        } else {
            return EvictionAttributes.createLRUHeapAttributes(ObjectSizer.DEFAULT, EvictionAction.parseAction(actionName));
        }
    }

    private EvictionAttributes getLruEntryCountEvictionAttributes(PdxInstance lruEntryCount) {
        Number maximumEntries = (Number) lruEntryCount.getField("maximum");
        String actionName = (String) lruEntryCount.getField("action");

        if (actionName == null) {
            if (maximumEntries == null) {
                return EvictionAttributes.createLRUEntryAttributes();
            }
            return EvictionAttributes.createLRUEntryAttributes(maximumEntries.intValue());
        }
        return EvictionAttributes.createLRUEntryAttributes(maximumEntries.intValue(), EvictionAction.parseAction(actionName));
    }

    @Override
    protected String getFieldName() {
        return "evictionAttributes";
    }
}

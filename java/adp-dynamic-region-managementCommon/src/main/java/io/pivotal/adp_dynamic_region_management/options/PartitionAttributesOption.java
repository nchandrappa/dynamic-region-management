package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.FixedPartitionAttributes;
import com.gemstone.gemfire.cache.PartitionAttributes;
import com.gemstone.gemfire.cache.PartitionAttributesFactory;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

import java.util.List;

public class PartitionAttributesOption extends RegionOption<PartitionAttributes> {
    public PartitionAttributesOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    @Override
    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        regionFactory.setPartitionAttributes(value());
    }

    @Override
    protected PartitionAttributes value() throws RegionOptionsInvalidException {
        PdxInstance attributes = (PdxInstance) serverOptions.getField(getFieldName());

        PartitionAttributesFactory factory = new PartitionAttributesFactory();
        setTotalNumBuckets(attributes, factory);
        setColocatedWith(attributes, factory);
        setLocalMaxMemory(attributes, factory);
        setRecoveryDelay(attributes, factory);
        setRedundantCopies(attributes, factory);
        setStartupRecoveryDelay(attributes, factory);
        setTotalMaxMemory(attributes, factory);
        setTotalNumBuckets(attributes, factory);
        setAllFixedPartitionAttributes(attributes, factory);

        try {
            return factory.create();
        } catch (IllegalStateException exception) {
            throw new RegionOptionsInvalidException(
                    String.format("Unable to set partitionAttributes: %s", exception.getMessage()), exception);
        }
    }

    private void setAllFixedPartitionAttributes(PdxInstance attributes, PartitionAttributesFactory factory) {
        List fpas = (List) attributes.getField("fixedPartitionAttributes");

        if (fpas != null) {
            for (Object fpa : fpas) {
                PdxInstance fixedPartitionAttributes = (PdxInstance)fpa;
                setFixedPartitionAttributes(fixedPartitionAttributes, factory);
            }
        }
    }

    private void setFixedPartitionAttributes(PdxInstance pdxInstance, PartitionAttributesFactory factory) {
        String partitionName = (String)pdxInstance.getField("partitionName");
        Boolean isDefault = (Boolean)pdxInstance.getField("isDefault");
        Number numBuckets = (Number)pdxInstance.getField("numBuckets");

        FixedPartitionAttributes fixedPartitionAttributes;

        if (numBuckets == null) {
            if (isDefault == null) {
                fixedPartitionAttributes = FixedPartitionAttributes.createFixedPartition(partitionName);
            } else {
                fixedPartitionAttributes = FixedPartitionAttributes.createFixedPartition(partitionName, isDefault);
            }
        } else {
            if (isDefault == null) {
                fixedPartitionAttributes = FixedPartitionAttributes.createFixedPartition(partitionName, numBuckets.intValue());
            } else {
                fixedPartitionAttributes = FixedPartitionAttributes.createFixedPartition(partitionName, isDefault, numBuckets.intValue());
            }
        }

        factory.addFixedPartitionAttributes(fixedPartitionAttributes);
    }

    private void setLocalMaxMemory(PdxInstance attributes, PartitionAttributesFactory factory) {
        Number localMaxMemory = (Number) attributes.getField("localMaxMemory");
        if (localMaxMemory != null) {
            factory.setLocalMaxMemory(localMaxMemory.intValue());
        }
    }

    private void setColocatedWith(PdxInstance attributes, PartitionAttributesFactory factory) {
        String colocatedWith = (String) attributes.getField("colocatedWith");
        if (colocatedWith != null) {
            factory.setColocatedWith(colocatedWith);
        }
    }

    private void setTotalNumBuckets(PdxInstance attributes, PartitionAttributesFactory factory) {
        Number totalNumBuckets = (Number) attributes.getField("totalNumBuckets");
        if (totalNumBuckets != null) {
            factory.setTotalNumBuckets(totalNumBuckets.intValue());
        }
    }

    private void setRecoveryDelay(PdxInstance attributes, PartitionAttributesFactory factory) {
        Number recoveryDelay = (Number) attributes.getField("recoveryDelay");
        if (recoveryDelay != null) {
            factory.setRecoveryDelay(recoveryDelay.longValue());
        }
    }

    private void setTotalMaxMemory(PdxInstance attributes, PartitionAttributesFactory factory) {
        Number totalMaxMemory = (Number) attributes.getField("totalMaxMemory");
        if (totalMaxMemory != null) {
            factory.setTotalMaxMemory(totalMaxMemory.longValue());
        }
    }

    private void setStartupRecoveryDelay(PdxInstance attributes, PartitionAttributesFactory factory) {
        Number startupRecoveryDelay = (Number) attributes.getField("startupRecoveryDelay");
        if (startupRecoveryDelay != null) {
            factory.setStartupRecoveryDelay(startupRecoveryDelay.longValue());
        }
    }

    private void setRedundantCopies(PdxInstance attributes, PartitionAttributesFactory factory) {
        Number redundantCopies = (Number) attributes.getField("redundantCopies");
        if (redundantCopies != null) {
            factory.setRedundantCopies(redundantCopies.intValue());
        }
    }

    @Override
    protected String getFieldName() {
        return "partitionAttributes";
    }
}

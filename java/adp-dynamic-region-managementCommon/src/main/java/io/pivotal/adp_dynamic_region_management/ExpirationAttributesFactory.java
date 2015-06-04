package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.ExpirationAction;
import com.gemstone.gemfire.cache.ExpirationAttributes;
import com.gemstone.gemfire.pdx.PdxInstance;

public class ExpirationAttributesFactory {
    private final PdxInstance pdxInstance;

    public ExpirationAttributesFactory(PdxInstance pdxInstance) {
        this.pdxInstance = pdxInstance;
    }

    public ExpirationAttributes create() throws RegionOptionsInvalidException {
        int expirationTime = expirationTime();
        ExpirationAction expirationAction = action();

        if(expirationAction == null) {
            return new ExpirationAttributes(expirationTime);
        } else {
            return new ExpirationAttributes(expirationTime, expirationAction);
        }

    }

    private ExpirationAction action() throws RegionOptionsInvalidException {
        String actionName = (String) pdxInstance.getField("action");

        if(actionName == null) {
            return null;
        }

        try {
            byte index = 0;
            while (true) {
                ExpirationAction possibleExpirationAction = ExpirationAction.fromOrdinal(index);
                if (possibleExpirationAction.toString().equals(actionName)) {
                    return possibleExpirationAction;
                }
                index++;
            }
        } catch(ArrayIndexOutOfBoundsException exception) {
            throw new RegionOptionsInvalidException(String.format("Invalid action: `%s`.", actionName));
        }
    }

    private int expirationTime() throws RegionOptionsInvalidException {
        Number timeout = (Number) pdxInstance.getField("timeout");
        if(timeout == null) {
            throw new RegionOptionsInvalidException("Expected timeout but none was provided.");
        }
        return timeout.intValue();
    }

}

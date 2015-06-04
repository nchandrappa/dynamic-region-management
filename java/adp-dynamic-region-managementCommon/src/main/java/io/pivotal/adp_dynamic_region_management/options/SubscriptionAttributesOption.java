package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.InterestPolicy;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.SubscriptionAttributes;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

public class SubscriptionAttributesOption extends RegionOption<SubscriptionAttributes> {
    public SubscriptionAttributesOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    @Override
    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        SubscriptionAttributes subscriptionAttributes = value();
        regionFactory.setSubscriptionAttributes(subscriptionAttributes);
    }

    @Override
    protected SubscriptionAttributes value() throws RegionOptionsInvalidException {
        PdxInstance subscriptionAttributes = (PdxInstance) serverOptions.getField(getFieldName());

        String interestPolicyName = (String) subscriptionAttributes.getField("interestPolicy");

        if (interestPolicyName != null) {
            byte index = 0;
            while (true) {
                try {
                    InterestPolicy possibleInterestPolicy = InterestPolicy.fromOrdinal(index);
                    if (possibleInterestPolicy.toString().equals(interestPolicyName)) {
                        return new SubscriptionAttributes(possibleInterestPolicy);
                    }
                    index++;
                } catch (ArrayIndexOutOfBoundsException exception) {
                    throw new RegionOptionsInvalidException(String.format("Invalid interestPolicy: `%s`.", interestPolicyName));
                }
            }
        }

        return new SubscriptionAttributes();
    }


    @Override
    protected String getFieldName() {
        return "subscriptionAttributes";
    }
}

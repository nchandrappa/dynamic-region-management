package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.LossAction;
import com.gemstone.gemfire.cache.MembershipAttributes;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.ResumptionAction;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

import java.util.List;

public class MembershipAttributesOption extends RegionOption<MembershipAttributes> {
    public MembershipAttributesOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    @Override
    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        regionFactory.setMembershipAttributes(value());
    }

    @Override
    protected MembershipAttributes value() throws RegionOptionsInvalidException {
        PdxInstance instance = (PdxInstance) serverOptions.getField(getFieldName());

        List<String> requiredRolesList = (List<String>) instance.getField("requiredRoles");

        if (requiredRolesList == null) {
            return new MembershipAttributes();
        }

        String[] requiredRoles = (requiredRolesList).toArray(new String[requiredRolesList.size()]);
        String lossActionName = (String) instance.getField("lossAction");
        String resumptionActionName = (String) instance.getField("resumptionAction");

        if (lossActionName == null && resumptionActionName == null) {
            return new MembershipAttributes(requiredRoles);
        } else if (lossActionName == null && resumptionActionName != null) {
            throw new RegionOptionsInvalidException(
                    "When a resumptionAction is given, a lossAction is required.");
        } else if (lossActionName != null && resumptionActionName == null) {
            throw new RegionOptionsInvalidException(
                    "When a lossAction is given, a resumptionAction is required.");
        }

        LossAction lossAction = LossAction.fromName(lossActionName);
        ResumptionAction resumptionAction = ResumptionAction.fromName(resumptionActionName);

        return new MembershipAttributes(requiredRoles, lossAction, resumptionAction);
    }

    @Override
    protected String getFieldName() {
        return "membershipAttributes";
    }
}

package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.distributed.internal.membership.InternalRole;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.CacheSingleton;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MembershipAttributesOptionTest {
    private static Cache cache;
    private static RegionFactory regionFactory;

    @Rule
    public TestName name = new TestName();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    static public void setUp() throws Exception {
        cache = CacheSingleton.getCache();
        regionFactory = CacheFactory.getAnyInstance().createRegionFactory();
    }

    @Test
    public void testSetsMembershipAttributes() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"membershipAttributes\": {" +
                "   \"requiredRoles\": [\"producer\"], " +
                "   \"lossAction\": \"NO_ACCESS\", " +
                "   \"resumptionAction\": \"REINITIALIZE\" " +
                "} }");

        new MembershipAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        MembershipAttributes membershipAttributes = region.getAttributes().getMembershipAttributes();

        assertThat(membershipAttributes.getLossAction(), equalTo(LossAction.NO_ACCESS));
        assertThat(membershipAttributes.getResumptionAction(), equalTo(ResumptionAction.REINITIALIZE));

        Set requiredRoles = new HashSet<InternalRole>();
        requiredRoles.add(InternalRole.getRole("producer"));
        assertThat(membershipAttributes.getRequiredRoles(), equalTo(requiredRoles));
    }

    @Test
    public void testSetsPartitionAttributesForEmptyObject() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON("{ \"membershipAttributes\": {} }");

        new MembershipAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        assertThat(region.getAttributes().getMembershipAttributes(), equalTo(new MembershipAttributes()));
    }

    @Test
    public void testSetsPartitionAttributesForObjectWithOnlyRequiredRoles() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"membershipAttributes\": {" +
                        "   \"requiredRoles\": [\"producer\"] " +
                        "} }");

        new MembershipAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        assertThat(region.getAttributes().getMembershipAttributes(), equalTo(new MembershipAttributes(new String[] {"producer"})));
    }

    @Test
    public void testSetsPartitionAttributesForObjectMissingLossAction() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"membershipAttributes\": {" +
                        "   \"requiredRoles\": [\"producer\"], " +
                        "   \"resumptionAction\": \"REINITIALIZE\" " +
                        "} }");


        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("When a resumptionAction is given, a lossAction is required.");

        new MembershipAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testSetsPartitionAttributesForObjectMissingResumptionAction() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"membershipAttributes\": {" +
                        "   \"requiredRoles\": [\"producer\"], " +
                        "   \"lossAction\": \"NO_ACCESS\" " +
                        "} }");


        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("When a lossAction is given, a resumptionAction is required.");

        new MembershipAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testIsAnOptionReturnsFalse() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{}");
        assertThat(new MembershipAttributesOption(serverOptions).isAnOption(), equalTo(false));
    }

    @Test
    public void testIsAnOptionReturnsTrue() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{\"membershipAttributes\": null}");
        assertThat(new MembershipAttributesOption(serverOptions).isAnOption(), equalTo(true));
    }

    private String getCurrentTestName() {
        return getClass().getName() + name.getMethodName();
    }
}
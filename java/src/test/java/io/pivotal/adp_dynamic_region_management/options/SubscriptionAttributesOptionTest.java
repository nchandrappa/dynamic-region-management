package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.CacheSingleton;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SubscriptionAttributesOptionTest {
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
    public void testSetsSubscriptionAttributes() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"subscriptionAttributes\": {" +
                        "   \"interestPolicy\": \"ALL\" " +
                        "} }");

        new SubscriptionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        SubscriptionAttributes subscriptionAttributes = region.getAttributes().getSubscriptionAttributes();

        assertThat(subscriptionAttributes, equalTo(new SubscriptionAttributes(InterestPolicy.ALL)));
    }

    @Test
    public void testSetsSubscriptionAttributesWithNoInterestPolicy() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON("{ \"subscriptionAttributes\": {} }");

        new SubscriptionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        SubscriptionAttributes subscriptionAttributes = region.getAttributes().getSubscriptionAttributes();

        assertThat(subscriptionAttributes, equalTo(new SubscriptionAttributes()));
    }

    @Test
    public void testSetsSubscriptionAttributesWithInvalidInterestPolicy() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"subscriptionAttributes\": {" +
                        "   \"interestPolicy\": \"ALL_OR_NOTHING\" " +
                        "} }");

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Invalid interestPolicy: `ALL_OR_NOTHING`.");

        new SubscriptionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testIsAnOptionReturnsFalse() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{}");
        assertThat(new SubscriptionAttributesOption(serverOptions).isAnOption(), equalTo(false));
    }

    @Test
    public void testIsAnOptionReturnsTrue() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{\"subscriptionAttributes\": null}");
        assertThat(new SubscriptionAttributesOption(serverOptions).isAnOption(), equalTo(true));
    }

    private String getCurrentTestName() {
        return getClass().getSimpleName() + name.getMethodName();
    }


}
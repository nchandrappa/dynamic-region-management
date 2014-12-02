package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class RegionOptionsFactoryTest {
    @BeforeClass
    static public void setUp() throws Exception {
        CacheSingleton.getCache();
    }

    @Rule
    public TestName name = new TestName();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetRegionFactoryWithEmptyOptions() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON("{}");

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);
        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();
        Region region = regionFactory.create(getCurrentTestName());

        assertThat(region.getAttributes().getDataPolicy(), equalTo(DataPolicy.DEFAULT));
    }

    @Test
    public void testRegionOptionsAreSetOnTheRegionFactory() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ " +
                        "\"asyncEventQueueIds\": [\"queue1\", \"queue2\"], " +
                        "\"cloningEnabled\": true, " +
                        "\"dataPolicy\": \"PERSISTENT_PARTITION\", " +
                        "\"diskStoreName\": \"bob\", " +
                        "\"diskSynchronous\": false, " +
                        "\"gatewaySenderIds\": [\"queue1\", \"queue2\"], " +
                        "\"ignoreJTA\": true, " +
                        "\"indexUpdateType\": \"synchronous\", " +
                        "\"initialCapacity\": 123, " +
                        "\"loadFactor\": 0.25, " +
                        "\"multicastEnabled\": true, " +
                        "\"statisticsEnabled\": true, " +
                        "\"keyConstraint\": \"java.lang.String\", " +
                        "\"valueConstraint\": \"java.lang.Integer\"," +
                        "\"partitionAttributes\": {" +
                        "   \"recoveryDelay\": 123, " +
                        "   \"redundantCopies\": 2, " +
                        "   \"fixedPartitionAttributes\": [" +
                        "        {" +
                        "           \"partitionName\": \"partition1\"," +
                        "           \"isDefault\": true," +
                        "           \"numBuckets\": 1" +
                        "        }" +
                        "   ]" +
                        "}," +
                        "\"membershipAttributes\": {" +
                        "   \"requiredRoles\": [\"producer\"] " +
                        "}," +
                        "\"subscriptionAttributes\": {" +
                        "   \"interestPolicy\": \"ALL\"" +
                        "}," +
                        "\"cacheLoader\": \"io.pivotal.adp_dynamic_region_management.FakeCacheLoader\", " +
                        "\"cacheWriter\": \"io.pivotal.adp_dynamic_region_management.FakeCacheWriter\", " +
                        "\"cacheListener\": [\"io.pivotal.adp_dynamic_region_management.FakeCacheListener\"], " +
                        "\"compressor\": \"io.pivotal.adp_dynamic_region_management.FakeCompressor\", " +
                        "\"evictionAttributes\": {" +
                        "   \"lruEntryCount\": {" +
                        "       \"maximum\": 1234, " +
                        "       \"action\": \"overflow-to-disk\" " +
                        "   } " +
                        "}" +
                "}"
        );

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);
        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();

        Region region = regionFactory.create(getCurrentTestName());

        RegionAttributes regionAttributes = region.getAttributes();

        assertThat((Set<String>) regionAttributes.getAsyncEventQueueIds(), contains("queue1", "queue2"));
        assertThat(regionAttributes.getCloningEnabled(), equalTo(true));
        assertThat(regionAttributes.getDataPolicy().toString(), equalTo("PERSISTENT_PARTITION"));
        assertThat(regionAttributes.getDiskStoreName(), equalTo("bob"));
        assertThat(regionAttributes.isDiskSynchronous(), equalTo(false));
        assertThat((Set<String>) regionAttributes.getGatewaySenderIds(), contains("queue1", "queue2"));
        assertThat(regionAttributes.getIgnoreJTA(), equalTo(true));
        assertThat(regionAttributes.getIndexMaintenanceSynchronous(), equalTo(true));
        assertThat(regionAttributes.getInitialCapacity(), equalTo(123));
        assertThat(regionAttributes.getLoadFactor(), equalTo(0.25f));
        assertThat(regionAttributes.getMulticastEnabled(), equalTo(true));
        assertThat(regionAttributes.getStatisticsEnabled(), equalTo(true));
        assertEquals(Class.forName(String.class.getName()), regionAttributes.getKeyConstraint());
        assertEquals(Class.forName(Integer.class.getName()), regionAttributes.getValueConstraint());


        PartitionAttributesFactory partitionAttributesFactory = new PartitionAttributesFactory();
        partitionAttributesFactory.setRecoveryDelay(123);
        partitionAttributesFactory.setRedundantCopies(2);
        partitionAttributesFactory.addFixedPartitionAttributes(
                FixedPartitionAttributes.createFixedPartition("partition1", true, 1));
        PartitionAttributes partitionAttributes = partitionAttributesFactory.create();
        assertThat(regionAttributes.getPartitionAttributes(),
                equalTo(partitionAttributes));

        assertThat(regionAttributes.getMembershipAttributes(), equalTo(new MembershipAttributes(new String[] { "producer" })));
        assertThat(regionAttributes.getSubscriptionAttributes(), equalTo(new SubscriptionAttributes(InterestPolicy.ALL)));
        assertEquals(FakeCacheLoader.class, regionAttributes.getCacheLoader().getClass());
        assertEquals(FakeCacheWriter.class, regionAttributes.getCacheWriter().getClass());
        assertEquals(FakeCacheListener.class, regionAttributes.getCacheListeners()[0].getClass());
        assertEquals(FakeCompressor.class, regionAttributes.getCompressor().getClass());

        EvictionAttributes evictionAttributes = EvictionAttributes.createLRUEntryAttributes(1234, EvictionAction.OVERFLOW_TO_DISK);
        assertThat(regionAttributes.getEvictionAttributes(), equalTo(evictionAttributes));
    }

    @Test
    public void testOtherRegionOptionsAreSetOnTheRegionFactory() throws Exception {
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ " +
                        "\"concurrencyChecksEnabled\": false, " +
                        "\"isLockGrantor\": true, " +
                        "\"scope\": \"GLOBAL\"," +
                        "\"regionTimeToLive\": {\"timeout\": 1234}, " +
                        "\"regionIdleTime\": {\"timeout\": 2345}, " +
                        "\"entryTimeToLive\": {\"timeout\": 3456}, " +
                        "\"entryIdleTime\": {\"timeout\": 4567} " +
                        "}"
        );

        RegionOptionsFactory regionOptionsFactory = new RegionOptionsFactory(serverOptions);
        RegionFactory regionFactory = regionOptionsFactory.getRegionFactory();

        Region region = regionFactory.create(getCurrentTestName());

        assertThat(region.getAttributes().getConcurrencyChecksEnabled(), equalTo(false));
        assertThat(region.getAttributes().isLockGrantor(), equalTo(true));
        assertThat(region.getAttributes().getScope(), equalTo(Scope.GLOBAL));
        assertThat(region.getAttributes().getRegionTimeToLive().getTimeout(), equalTo(1234));
        assertThat(region.getAttributes().getRegionIdleTimeout().getTimeout(), equalTo(2345));
        assertThat(region.getAttributes().getEntryTimeToLive().getTimeout(), equalTo(3456));
        assertThat(region.getAttributes().getEntryIdleTimeout().getTimeout(), equalTo(4567));
    }

    private String getCurrentTestName() {
        return name.getMethodName();
    }

}
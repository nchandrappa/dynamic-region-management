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

public class PartitionAttributesOptionTest {
    private static Cache cache;

    @Rule
    public TestName name = new TestName();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    static public void setUp() throws Exception {
        cache = CacheSingleton.getCache();
    }

    @Test
    public void testSetsPartitionAttributes() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        String colocatedRegionName = "testSetsPartitionAttributesOtherRegionName";
        RegionFactory<Object, Object> colocatedRegionFactory = cache.createRegionFactory(RegionShortcut.PARTITION);
        PartitionAttributesFactory colocatedPartitionAttributesFactory = new PartitionAttributesFactory();
        colocatedPartitionAttributesFactory.setRedundantCopies(2);
        colocatedPartitionAttributesFactory.setTotalNumBuckets(60);
        colocatedRegionFactory.setPartitionAttributes(colocatedPartitionAttributesFactory.create());
        colocatedRegionFactory.create(colocatedRegionName);

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"partitionAttributes\": {" +
                        "\"colocatedWith\": \"" + colocatedRegionName + "\", " +
                        "\"localMaxMemory\": 64, " +
                        "\"recoveryDelay\": 5, " +
                        "\"redundantCopies\": 2, " +
                        "\"startupRecoveryDelay\": 3, " +
                        "\"totalMaxMemory\": 128, " +
                        "\"totalNumBuckets\": 60 " +
                        "} }");

        new PartitionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        PartitionAttributes partitionAttributes = region.getAttributes().getPartitionAttributes();

        assertThat(partitionAttributes.getColocatedWith(), equalTo(colocatedRegionName));
        assertThat(partitionAttributes.getLocalMaxMemory(), equalTo(64));
        assertThat(partitionAttributes.getRecoveryDelay(), equalTo(5L));
        assertThat(partitionAttributes.getRedundantCopies(), equalTo(2));
        assertThat(partitionAttributes.getStartupRecoveryDelay(), equalTo(3L));
        assertThat(partitionAttributes.getTotalMaxMemory(), equalTo(128L));
        assertThat(partitionAttributes.getTotalNumBuckets(), equalTo(60));
    }

    @Test
    public void testSetsFixedPartitionAttributes() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"partitionAttributes\": {" +
                        "\"redundantCopies\": 2, " +
                        "\"fixedPartitionAttributes\": [" +
                        "    {" +
                        "        \"partitionName\": \"partition1\"" +
                        "    }," +
                        "    {" +
                        "        \"partitionName\": \"partition2\"," +
                        "        \"isDefault\": false" +
                        "    }," +
                        "    {" +
                        "        \"partitionName\": \"partition3\"," +
                        "        \"isDefault\": false," +
                        "        \"numBuckets\": 2" +
                        "    }," +
                        "    {" +
                        "        \"partitionName\": \"partition4\"," +
                        "        \"numBuckets\": 2" +
                        "    }" +
                        "  ]" +
                        "} }");

        new PartitionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        PartitionAttributes partitionAttributes = region.getAttributes().getPartitionAttributes();

        FixedPartitionAttributes first = (FixedPartitionAttributes) partitionAttributes.getFixedPartitionAttributes().get(0);
        assertThat(first, equalTo(FixedPartitionAttributes.createFixedPartition("partition1")));

        FixedPartitionAttributes second = (FixedPartitionAttributes) partitionAttributes.getFixedPartitionAttributes().get(1);
        assertThat(second, equalTo(FixedPartitionAttributes.createFixedPartition("partition2", false)));

        FixedPartitionAttributes third = (FixedPartitionAttributes) partitionAttributes.getFixedPartitionAttributes().get(2);
        assertThat(third, equalTo(FixedPartitionAttributes.createFixedPartition("partition3", false, 2)));

        FixedPartitionAttributes fourth = (FixedPartitionAttributes) partitionAttributes.getFixedPartitionAttributes().get(3);
        assertThat(fourth, equalTo(FixedPartitionAttributes.createFixedPartition("partition4", 2)));
    }

    @Test
    public void testSetFixedPartitionAttributesFailsWhenNoAttributesExist() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"partitionAttributes\": {\"fixedPartitionAttributes\": [{}] } }");

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Fixed partition name cannot be null");

        new PartitionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testSetsPartitionAttributesForEmptyObject() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON("{ \"partitionAttributes\": {} }");

        new PartitionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        PartitionAttributes partitionAttributes = region.getAttributes().getPartitionAttributes();

        assertThat(partitionAttributes, equalTo(new PartitionAttributesFactory().create()));
    }

    @Test
    public void testIsAnOptionReturnsFalse() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{}");
        assertThat(new PartitionAttributesOption(serverOptions).isAnOption(), equalTo(false));
    }

    @Test
    public void testIsAnOptionReturnsTrue() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{\"partitionAttributes\": null}");
        assertThat(new PartitionAttributesOption(serverOptions).isAnOption(), equalTo(true));
    }

    private String getCurrentTestName() {
        return getClass().getSimpleName() + name.getMethodName();
    }
}
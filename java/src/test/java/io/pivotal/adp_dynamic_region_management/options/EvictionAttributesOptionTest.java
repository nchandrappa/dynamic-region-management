package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.cache.util.ObjectSizer;
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

public class EvictionAttributesOptionTest {
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
    public void testSetsEvictionAttributesWithLruEntryCount() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"evictionAttributes\": {" +
                        "    \"lruEntryCount\": {" +
                        "       \"maximum\": 1234, " +
                        "       \"action\": \"overflow-to-disk\" " +
                        "    } " +
                        "  } " +
                        "}");

        new EvictionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        EvictionAttributes evictionAttributes = region.getAttributes().getEvictionAttributes();

        assertThat(evictionAttributes, equalTo(EvictionAttributes.createLRUEntryAttributes(1234, EvictionAction.OVERFLOW_TO_DISK)));
    }

    @Test
    public void testSetsEvictionAttributesWithLruEntryCountWithoutAction() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"evictionAttributes\": {" +
                        "    \"lruEntryCount\": {" +
                        "       \"maximum\": 1234 " +
                        "    } " +
                        "  } " +
                        "}");

        new EvictionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        EvictionAttributes evictionAttributes = region.getAttributes().getEvictionAttributes();

        assertThat(evictionAttributes, equalTo(EvictionAttributes.createLRUEntryAttributes(1234)));
    }

    @Test
    public void testSetsEvictionAttributesWithLruEntryCountWithoutParameters() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"evictionAttributes\": {" +
                        "    \"lruEntryCount\": {" +
                        "    } " +
                        "  } " +
                        "}");

        new EvictionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        EvictionAttributes evictionAttributes = region.getAttributes().getEvictionAttributes();

        assertThat(evictionAttributes, equalTo(EvictionAttributes.createLRUEntryAttributes()));
    }

    @Test
    public void testSetsEvictionAttributesWithLruMemorySize() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"evictionAttributes\": {" +
                        "    \"lruMemorySize\": {" +
                        "       \"maximum\": 1234, " +
                        "       \"action\": \"overflow-to-disk\" " +
                        "    } " +
                        "  } " +
                        "}");

        new EvictionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        EvictionAttributes evictionAttributes = region.getAttributes().getEvictionAttributes();

        assertThat(evictionAttributes, equalTo(EvictionAttributes.createLRUMemoryAttributes(1234, ObjectSizer.DEFAULT, EvictionAction.OVERFLOW_TO_DISK)));
    }

    @Test
    public void testSetsEvictionAttributesWithLruMemorySizeWithoutAction() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"evictionAttributes\": {" +
                        "    \"lruMemorySize\": {" +
                        "       \"maximum\": 1234 " +
                        "    } " +
                        "  } " +
                        "}");

        new EvictionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        EvictionAttributes evictionAttributes = region.getAttributes().getEvictionAttributes();

        assertThat(evictionAttributes, equalTo(EvictionAttributes.createLRUMemoryAttributes(1234)));
    }

    @Test
    public void testSetsEvictionAttributesWithLruMemorySizeWithoutMaximum() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"evictionAttributes\": {" +
                        "    \"lruMemorySize\": {" +
                        "       \"action\": \"overflow-to-disk\" " +
                        "    } " +
                        "  } " +
                        "}");

        new EvictionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        EvictionAttributes evictionAttributes = region.getAttributes().getEvictionAttributes();

        assertThat(evictionAttributes, equalTo(EvictionAttributes.createLRUMemoryAttributes(ObjectSizer.DEFAULT, EvictionAction.OVERFLOW_TO_DISK)));
    }

    @Test
    public void testSetsEvictionAttributesWithLruMemorySizeWithoutParameters() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"evictionAttributes\": {" +
                        "    \"lruMemorySize\": {" +
                        "    } " +
                        "  } " +
                        "}");

        new EvictionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        EvictionAttributes evictionAttributes = region.getAttributes().getEvictionAttributes();

        assertThat(evictionAttributes, equalTo(EvictionAttributes.createLRUMemoryAttributes()));
    }

    @Test
    public void testSetsEvictionAttributesWithLruHeapPercentageWithoutParameters() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"evictionAttributes\": {" +
                        "    \"lruHeapPercentage\": {" +
                        "    } " +
                        "  } " +
                        "}");

        new EvictionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        EvictionAttributes evictionAttributes = region.getAttributes().getEvictionAttributes();

        assertThat(evictionAttributes, equalTo(EvictionAttributes.createLRUHeapAttributes()));
    }

    @Test
    public void testSetsEvictionAttributesWithLruHeapPercentageWithAction() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"evictionAttributes\": {" +
                        "    \"lruHeapPercentage\": {" +
                        "       \"action\": \"overflow-to-disk\"" +
                        "    } " +
                        "  } " +
                        "}");

        new EvictionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        EvictionAttributes evictionAttributes = region.getAttributes().getEvictionAttributes();

        assertThat(evictionAttributes, equalTo(EvictionAttributes.createLRUHeapAttributes(ObjectSizer.DEFAULT, EvictionAction.OVERFLOW_TO_DISK)));
    }

    @Test
    public void testSetsEvictionAttributesWithAnInvalidAlgorithm() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"evictionAttributes\": {" +
                        "    \"invalidAlgorithm\": {" +
                        "    } " +
                        "  } " +
                        "}");

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Invalid evictionAttributes algorithm: [invalidAlgorithm]");

        new EvictionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testSetsEvictionAttributesWithMultipleAlgorithms() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"evictionAttributes\": {" +
                        "    \"lruHeapPercentage\": {}," +
                        "    \"lruMemorySize\": {}" +
                        "  } " +
                        "}");

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("You must specify only one algorithm for evictionAttributes: [lruHeapPercentage, lruMemorySize]");

        new EvictionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testSetsEvictionAttributesWithEmptyObject() throws Exception {
        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

        PdxInstance serverOptions = JSONFormatter.fromJSON("{ \"evictionAttributes\": {} }");

        new EvictionAttributesOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        EvictionAttributes evictionAttributes = region.getAttributes().getEvictionAttributes();

        assertThat(evictionAttributes.getAlgorithm(), equalTo(EvictionAlgorithm.NONE));
    }

    @Test
    public void testIsAnOptionReturnsFalse() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{}");
        assertThat(new EvictionAttributesOption(serverOptions).isAnOption(), equalTo(false));
    }

    @Test
    public void testIsAnOptionReturnsTrue() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{\"evictionAttributes\": null}");
        assertThat(new EvictionAttributesOption(serverOptions).isAnOption(), equalTo(true));
    }

    private String getCurrentTestName() {
        return getClass().getName() + name.getMethodName();
    }
}
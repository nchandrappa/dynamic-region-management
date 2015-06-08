package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;

import io.pivotal.adp_dynamic_region_management.CacheSingleton;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;
import io.pivotal.adp_dynamic_region_management.options.EntryIdleTimeOption;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EntryIdleTimeOptionTest {
    @Rule
    public TestName name = new TestName();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testSetOptionOnRegionFactory() throws Exception {
        CacheSingleton.getCache();

        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"entryIdleTime\": {\"timeout\": 1234, \"action\": \"DESTROY\"} }");

        new EntryIdleTimeOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        ExpirationAttributes expectedEntryIdleTime = new ExpirationAttributes(1234, ExpirationAction.DESTROY);

        assertThat(region.getAttributes().getEntryIdleTimeout(), equalTo(expectedEntryIdleTime));
    }

    @Test
    public void testSetOptionOnRegionFactoryFailsWhenActionIsNotFound() throws Exception {
        CacheSingleton.getCache();

        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();
        PdxInstance serverOptions = JSONFormatter.fromJSON(
                "{ \"entryIdleTime\": {\"timeout\": 1234, \"action\": \"NO_EXIST\"} }");

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Invalid action: `NO_EXIST`.");

        new EntryIdleTimeOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testSetOptionOnRegionFactoryFailsWhenActionIsNotProvided() throws Exception {
        CacheSingleton.getCache();

        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{ \"entryIdleTime\": {\"timeout\": 1234} }");

        new EntryIdleTimeOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        ExpirationAttributes expectedEntryIdleTime = new ExpirationAttributes(1234, ExpirationAction.INVALIDATE);

        assertThat(region.getAttributes().getEntryIdleTimeout(), equalTo(expectedEntryIdleTime));
    }

    @Test
    public void testSetOptionOnRegionFactoryFailsWhenTimeoutIsNotProvided() throws Exception {
        CacheSingleton.getCache();

        RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{ \"entryIdleTime\": {\"action\": \"DESTROY\"} }");

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Expected timeout but none was provided.");

        new EntryIdleTimeOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    @Test
    public void testIsAnOptionReturnsFalse() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{}");
        assertThat(new EntryIdleTimeOption(serverOptions).isAnOption(), equalTo(false));
    }

    @Test
    public void testIsAnOptionReturnsTrue() throws Exception {
        CacheSingleton.getCache();
        PdxInstance serverOptions = JSONFormatter.fromJSON("{\"entryIdleTime\": null}");
        assertThat(new EntryIdleTimeOption(serverOptions).isAnOption(), equalTo(true));
    }

    private String getCurrentTestName() {
        return getClass().getSimpleName() + name.getMethodName() + "EntryIdleTime";
    }
}
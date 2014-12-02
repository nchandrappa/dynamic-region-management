package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.options.*;

import java.util.Arrays;
import java.util.List;

public class RegionOptionsFactory {
    private Cache cache;
    private PdxInstance serverOptions;

    public RegionOptionsFactory(PdxInstance serverOptions) {
        this.serverOptions = serverOptions;
        this.cache = CacheFactory.getAnyInstance();
    }

    public RegionFactory getRegionFactory() {
        try {
            return buildRegionFactory();
        } catch (RegionOptionsInvalidException e) {
            return null;
        }
    }

    public void validate() throws RegionOptionsInvalidException {
        buildRegionFactory();
    }

    private RegionFactory buildRegionFactory() throws RegionOptionsInvalidException {
        if (serverOptions == null) {
            return this.cache.createRegionFactory();
        }

        RegionShortcut regionShortcut;

        String serverRegionType = (String) serverOptions.getField("type");
        if (serverRegionType == null) {
            regionShortcut = null;
        } else {
            regionShortcut = RegionShortcut.valueOf(serverRegionType);
        }

        RegionFactory regionFactory;
        if (regionShortcut != null) {
            regionFactory = this.cache.createRegionFactory(regionShortcut);
        } else {
            regionFactory = this.cache.createRegionFactory();
        }

        List<RegionOption<? extends Object>> regionOptions = Arrays.asList(
                new InitialCapacityOption(serverOptions),
                new ConcurrencyLevelOption(serverOptions),
                new DataPolicyOption(serverOptions),
                new EnableAsyncConflationOption(serverOptions),
                new EnableSubscriptionConflationOption(serverOptions),
                new GatewaySenderIdsOption(serverOptions),
                new AsyncEventQueueIdsOption(serverOptions),
                new IgnoreJTAOption(serverOptions),
                new IndexUpdateTypeOption(serverOptions),
                new IsLockGrantorOption(serverOptions),
                new LoadFactorOption(serverOptions),
                new MulticastEnabledOption(serverOptions),
                new DiskStoreNameOption(serverOptions),
                new DiskSynchronousOption(serverOptions),
                new StatisticsEnabledOption(serverOptions),
                new CloningEnabledOption(serverOptions),
                new ConcurrencyChecksEnabledOption(serverOptions),
                new ScopeOption(serverOptions),
                new KeyConstraintOption(serverOptions),
                new ValueConstraintOption(serverOptions),
                new RegionTimeToLiveOption(serverOptions),
                new RegionIdleTimeOption(serverOptions),
                new EntryTimeToLiveOption(serverOptions),
                new EntryIdleTimeOption(serverOptions),
                new PartitionAttributesOption(serverOptions),
                new MembershipAttributesOption(serverOptions),
                new SubscriptionAttributesOption(serverOptions),
                new CacheLoaderOption(serverOptions),
                new CacheWriterOption(serverOptions),
                new CacheListenerOption(serverOptions),
                new CompressorOption(serverOptions),
                new EvictionAttributesOption(serverOptions)
        );

        for (RegionOption regionOption : regionOptions) {
            if (regionOption.isAnOption()) {
                regionOption.setOptionOnRegionFactory(regionFactory);
            }
        }

        return regionFactory;
    }
}

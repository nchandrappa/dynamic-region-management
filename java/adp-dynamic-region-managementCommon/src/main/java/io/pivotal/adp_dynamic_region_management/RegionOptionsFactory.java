package io.pivotal.adp_dynamic_region_management;

import io.pivotal.adp_dynamic_region_management.options.AsyncEventQueueIdsOption;
import io.pivotal.adp_dynamic_region_management.options.CacheListenerOption;
import io.pivotal.adp_dynamic_region_management.options.CacheLoaderOption;
import io.pivotal.adp_dynamic_region_management.options.CacheWriterOption;
import io.pivotal.adp_dynamic_region_management.options.CloningEnabledOption;
import io.pivotal.adp_dynamic_region_management.options.CompressorOption;
import io.pivotal.adp_dynamic_region_management.options.ConcurrencyChecksEnabledOption;
import io.pivotal.adp_dynamic_region_management.options.ConcurrencyLevelOption;
import io.pivotal.adp_dynamic_region_management.options.DataPolicyOption;
import io.pivotal.adp_dynamic_region_management.options.DiskStoreNameOption;
import io.pivotal.adp_dynamic_region_management.options.DiskSynchronousOption;
import io.pivotal.adp_dynamic_region_management.options.EnableAsyncConflationOption;
import io.pivotal.adp_dynamic_region_management.options.EnableSubscriptionConflationOption;
import io.pivotal.adp_dynamic_region_management.options.EntryIdleTimeOption;
import io.pivotal.adp_dynamic_region_management.options.EntryTimeToLiveOption;
import io.pivotal.adp_dynamic_region_management.options.EvictionAttributesOption;
import io.pivotal.adp_dynamic_region_management.options.GatewaySenderIdsOption;
import io.pivotal.adp_dynamic_region_management.options.IgnoreJTAOption;
import io.pivotal.adp_dynamic_region_management.options.IndexUpdateTypeOption;
import io.pivotal.adp_dynamic_region_management.options.InitialCapacityOption;
import io.pivotal.adp_dynamic_region_management.options.IsLockGrantorOption;
import io.pivotal.adp_dynamic_region_management.options.KeyConstraintOption;
import io.pivotal.adp_dynamic_region_management.options.LoadFactorOption;
import io.pivotal.adp_dynamic_region_management.options.MembershipAttributesOption;
import io.pivotal.adp_dynamic_region_management.options.MulticastEnabledOption;
import io.pivotal.adp_dynamic_region_management.options.PartitionAttributesOption;
import io.pivotal.adp_dynamic_region_management.options.RegionIdleTimeOption;
import io.pivotal.adp_dynamic_region_management.options.RegionOption;
import io.pivotal.adp_dynamic_region_management.options.RegionTimeToLiveOption;
import io.pivotal.adp_dynamic_region_management.options.ScopeOption;
import io.pivotal.adp_dynamic_region_management.options.StatisticsEnabledOption;
import io.pivotal.adp_dynamic_region_management.options.SubscriptionAttributesOption;
import io.pivotal.adp_dynamic_region_management.options.ValueConstraintOption;

import java.util.Arrays;
import java.util.List;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.pdx.PdxInstance;

public class RegionOptionsFactory {
    private Cache cache;
    private PdxInstance serverOptions;
    private DistributionPolicy distributionPolicy;

    public RegionOptionsFactory(PdxInstance serverOptions, DistributionPolicy distributionPolicy) {
        this.serverOptions = serverOptions;
        this.distributionPolicy = distributionPolicy;
        this.cache = CacheFactory.getAnyInstance();
    }

    /**
     * this ctor with no distribution policy will create a RegionOptionsFactory that behaves
     * the same way it did before the DistirbutionPolicy Enhancement, thus enabling 
     * previously written tests to pass
     * 
     * @param serverOptions
     */
    public RegionOptionsFactory(PdxInstance serverOptions) {
        this.serverOptions = serverOptions;
        this.distributionPolicy = null;
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
                new GatewaySenderIdsOption(serverOptions, this.distributionPolicy), 
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

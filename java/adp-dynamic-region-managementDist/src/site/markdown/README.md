# Documentation

## Index

[Objectives] (objectives.md)

[Design] (design.md)

[Gateways] (gateways.md)

[Usage - Java Client] (usage/java_client.md)

[Usage - Native Client] (usage/native_client.md)

[Usage - Server] (usage/server.md)

[Implementation] (implementation.md)

[Known Issues] (known_issues.md)
  <gateway-sender id="DC2" parallel="false" remote-distributed-system-id="2" disk-store-name="adp-gemfire-store" enable-batch-conflation="true" enable-persistence="true" manual-start="true" />

  <disk-store name="adp-gemfire-store">
    <disk-dirs>
      <disk-dir>data</disk-dir>
    </disk-dirs>
  </disk-store>

  <pdx persistent="true" read-serialized="true" disk-store-name="adp-gemfire-store" />

  <!-- NON-SOR REGIONS - START -->
  <region name="__gemusers" refid="REPLICATE_PERSISTENT">
        <region-attributes  disk-store-name="adp-gemfire-store" gateway-sender-ids="DC2" enable-gateway="true" />
  </region>

  <region name="__regionAttributesMetadata" refid="REPLICATE_PERSISTENT_OVERFLOW">
    <region-attributes disk-store-name="adp-gemfire-store" gateway-sender-ids="DC2" enable-gateway="true">
      <cache-listener>
        <class-name>io.pivotal.adp_dynamic_region_management.MetadataRegionCacheListener</class-name>
        <parameter name="distributionPolicyClass">
                <string>io.pivotal.adp_dynamic_region_management.BasicDistributionPolicy</string>
        </parameter>
        <parameter name="GLOBAL_DISTRIBUTION_POLICY">
                <string>DC2</string>
        </parameter>
        <parameter name="REGIONAL_DISTRIBUTION_POLICY">
                <string>DC2</string>
        </parameter>
        <parameter name="DEFAULT_DISTRIBUTION_POLICY">
                <string>GLOBAL</string>
        </parameter>
      </cache-listener>
      <eviction-attributes>
        <lru-heap-percentage action="overflow-to-disk"/>
      </eviction-attributes>
    </region-attributes>
  </region>

# Usage : Server
There are three parts to the server-side configuration to use Dynamic Region Management.

## Start scripts
The start-up scripts need the following amendments. Stop scripts do not need changed.

### Cluster configuration
The cluster configuration service should be disabled on all processes, servers __and__ locators.

This is done with `--enable-cluster-configuration=false` for locators,
and with `--use-cluster-configuration=false` for servers.

The cluster configuration service runs on the locators, but clashes with Dynamic
Region Management.

### Server port
On server scripts, the port option `--server-port=` should not be specified.

The port is set later from a Java class.

### Default server
On server scripts, the flag `--disable-default-server` should be specified.

This stops the server from becoming available until Dynamic Region Management has
completed initialization and set the listener port.

### System properties
Three system properties should be provided to server scripts.

```
--J=-DCACHE_SERVER_PORT=
--J=-DGATEWAY_RECEIVER_START_PORT=
--J=-DGATEWAY_RECEIVER_END_PORT=
```

The cache server port is the port each JVM listens on for client connections. It
should be unique per host.

The gateway receiver start and end pairing define a range of ports that each server
can listen on for incoming connections. Each server picks one unused port from the
range, so if sufficient ports exist all servers can have the same start and end
ports on a host.

## Classpath
The Maven build process for Dynamic Region Management creates a single Jar file as part of the
distribution bundle.

This is found in the `java/adp-dynamic-region-managementDist/target` directory when the build
completes successfully.

The Jar file includes the text `server.jar` at the end, and will include the build version.

For example, for build 0.3.7 the file would be `adp-dynamic-region-managementDist-0.3.7-server.jar`,
for build 1.0.0 the file would be `adp-dynamic-region-managementDist-1.0.0-server.jar`, and
so on.

The file used needs to be present in the classpath for all servers in the cluster.

## "*cache.xml*" file
Several sections need to be added to "*cache.xml*" file used by the servers. This will typically make up the
bulk of the file, although other sections may be added for other requirements such as security.

### "*gateway-sender*"
One "*gateway-sender* should be defined for each remote cluster that data is to be sent to.

In this example, the sender is named "*DC2*" indicating it sends to datacenter 2, and the reference
number of that remote cluster "*remote-distributed-system-id*" is also 2. These two do not have to match.

Dispatch mode must be set for "*parallel*" deactivated and a single dispatcher thread, so that all events
arrive in the strict sequence that they were created. Multiple delivery channels can mean out of sequence
delivery, which can cause events to be delivered before the region needed to hold them exists.

Batch conflation must be deactivated. Conflation discards the first event when there are two for the same
key to reduce the volume of updates transmitted. This cannot be guaranteed to be valid for all applications,
which may need to see each update as they occur.

Persistence needs to be activated and this requires a disk store. This is so that any queued events are
saved to disk for later delivery after a cold start of the cluster.

Manual start is required, the "*initializer*" later in the "*cache.xml*" file starts the sending at
the optimal point in the server initialization sequence.

```
<gateway-sender 
 id="DC2"
 parallel="false"
 dispatcher-threads="1"
 remote-distributed-system-id="2"
 disk-store-name="adp-gemfire-store"
 enable-batch-conflation="false"
 enable-persistence="true"
 manual-start="true"
/>

```

### "*gateway-receiver*"
"*gateway-receiver*" sections should **NOT** be defined in XML.

These are added after by the "*cache-initializer*" section at a later point in the start-up sequence.

This is to ensure all regions are created, and to avoid a security deadlock, in the start-up sequence.

### "*disk-store*"
A single disk store is used per server for all parts of Dynamic Region Management : gateway queues,
PDX, the region metadata and the dynamically created persistent regions.

It is named here "*adp-gemfire-store*" and mapped onto the filesystem's `data` directory.

```
<disk-store name="adp-gemfire-store">
 <disk-dirs>
  <disk-dir>data</disk-dir>
 </disk-dirs>
</disk-store>
```

### "*pdx*"
Region metadata is defined as JSON held in PDX instance objects.

To support this, a PDX section needs to be provided. This needs "*read-serialized*" enabled, as
the PDX instance objects have no corresponding domain class. For persistence, the "*adp-disk-store*"
is used.

```
<pdx
 persistent="true"
 read-serialized="true"
 disk-store-name="adp-gemfire-store"
/>
```

### "*region*" for Metadata
One region needs to be defined, the metadata region that holds the configuration of
the other regions that need to be created.

This region must be named "*__regionAttributesMetadata*" to match the coding. Region
names which begin with two underscores is the convention for indicating them to be
for system housekeeping purposes rather than normal use.

Region attributes should be "*REPLICATE_PERSISTENT*" so that the region is visible on
all servers and saved to disk across a restart. For disk saving, the disk store name
must be given.

The region is gateway enabled, and needs to list the gateway sender names of all
remote clusters, with the names separated by commas.

A cache listener needs to be defined to act on changes to the metadata. Two parameters
which will likely need varied are "*GLOBAL_DISTRIBUTION_POLICY*" which lists the
remote clusters considered to be worldwide, and "*REGIONAL_DISTRIBUTION_POLICY*"
which lists the remote clusters considered to be in the same continent.

```
<region name="__regionAttributesMetadata">
 <region-attributes refid="REPLICATE_PERSISTENT"
  disk-store-name="adp-gemfire-store" 
  gateway-sender-ids="DC2" enable-gateway="true">
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
 </region-attributes>
</region>
```

### "*function-service*"
The region management functions "*CreateRegion*" and "*DestroyRegion*" need to be defined, along with any other
functions the server may need for other reasons.

```
<function-service>
 <function>
  <class-name>io.pivotal.adp_dynamic_region_management.CreateRegion</class-name>
 </function>
 <function>
  <class-name>io.pivotal.adp_dynamic_region_management.DestroyRegion</class-name>
 </function>
</function-service>
```

### "*initializer*"
An "*initializer*" section defines a Java class to run immediately prior to the
server becoming available, at the end of the start-up sequence.

This should be coded as below, and defines a parameter for the port the server
should listen on. If specified with a system property `${CACHE_SERVER_PORT}`
the actual value can be provided by the start-up scripts and the same "*cache.xml*"
file used by all servers.

```
<initializer>
 <class-name>io.pivotal.adp_dynamic_region_management.CacheInitializer</class-name>
 <parameter name="cacheServerPort">
  <string>${CACHE_SERVER_PORT}</string>
 </parameter>
</initializer>
```

## Start-up verification
Although the usual rules about checking for errors and warnings in the logs apply,
the presence of the following message is a good indication that start-up has
performed the necessary actions.

```
StartupConductor: startup sequence is complete
```

If the above is missing, and logging isn't turned off, most likely something
has gone wrong.
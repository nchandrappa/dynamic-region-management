# Implementation
Dynamic Region Management needs to be unaffected by cluster bounces, and to provide
mechanisms to add, remove and change regions.

## Cluster start
The metadata region is persistent. When the cluster starts, the metadata region contents
are recovered from the disk store automatically as part of Gemfire start-up.

The cache listener attached to the metadata region will receive an `afterRegionLive()`
event once the region contents are available but before the server as a whole is
available.

This listener iterates across the entries in the metadata region, which are the regions
that existed at the previous shutdown, and calls the create region mechanism to
recreate them.

### Dynamic region recreation
As the cluster starts, the listener on the metadata region triggers the creation call
for each required dynamic region as part of the creation step for the metadata region.

That is, the metadata region creation does not complete until all dynamic regions have
been created. All dynamic regions are recreated before the the metadata region is fully
available, which in turn is before the servers become available.

What this means in practice is all dynamic regions which used to exist are recreated
before the cluster is useable rather than just after. 

### Dynamic region persistence reload
In addition to recreating dynamic regions as part of the initialization sequence of
the cluster, recreating the dynamic regions prior to cluster availability gives them
access to the disk store reload.

What this means in practice is that data for the dynamic regions which was saved to
the disk stores is recovered as part of cache initialization.

So, when the cluster restarts, all dynamic regions which existed at shutdown are
rebuilt, and those dynamic regions which were configured for persistent data also
have that data restored.

## Region creation
On the server side, dynamic regions are created by invoking a Gemfire function.

### Function
The function is `io.pivotal.adp_dynamic_region_management.CreateRegion` but is
invoked by the simple class name "*CreateRegion*".

The action of the "*CreateRegion*" function is quite lightweight. Some simple
validation is performed on the function arguments, for example that the region
name given does not already exist, and then the function arguments are inserted
into the metadata region with the region name as the key and the region
attributes as the value.

In practice this validation means that Dynamic Region Management can create
any region that does not already exist.

### Event listener
After the metadata entry is inserted, an `afterUpdate()` listener is triggered
and calls the `createRegion()` method in the same class. This method creates
the dynamic region on the sevrer JVM.

This `createRegion()` method is the same one invoked by system start-up. There
is only one mechanism to create dynamic regions, although it can be triggered
from different points. This ensures regions created dynamically at restart
are no different from regions created dynamically once the cluster is running.

#### Region attributes
After the region name required, the main argument to the region creation
function is the region attributes to use.

This is specified as a list of options in JSON format. For example.

```
{"server": { "type": "REPLICATE", "cacheListener": ["com.adp.something.MyCacheListener"] } 
```

The above defines that the options used on the server side are to set the region
type to *PARTITION* and to add a cache listener with the given class. All other
options are defaulted.

All options can be omitted with the exception of the region type, for which no
default is provided. So a minimum request may look like this.

```
{"server": { "type": "PARTITION" }
```

#### Region attributes continued
Region attributes for clients are held on the server side, as client metadata
is not persistent.

So the minimal region options may be this:

```
{ {"client": { "type": "PROXY" , "poolName" : "myPool" } , {"server": { "type": "PARTITION" } }
```

The options specified for servers are used to build the region on the server side.

The options specified for clients are merely stored, and passed to clients when they
connect so that clients can build the appropriate regions when they start.

## Region deletion
Region deletion is the counterpart to region creation, and follows the
same implementation pattern.

### Function
A lightweight function `io.pivotal.adp_dynamic_region_management.DestroyRegion`
can be invoked by the name "*DestroyRegion*".

It takes the target region name as an argument, and deletes that entry from
the metadata if it can be found, and again leaves the main work to the
attached event listener.

As the entry is removed from the metadata by the function, Dynamic Region
Management can only remove regions that it has created. It cannot remove
other regions from the cluster.

### Event listener
When the metadata entry is removed by the function, the 'afterDestroy()'
event is triggered in the cache listener.

The cache listener code then removes region hosting responsibility from
each server JVM, and so deletes it from all server JVMs.
This uses the `Region.localDestroy()` method.

## Region update
To complete the picture, regions can be updated in principle.

This is not fully implemented, and exists as a placeholder for potential
future functionality.

It is unclear if there is any actual requirement to adjust region
configuration.

## Event listener
As the metadata region is defined as replicated, the contents are present
on all server JVMs in the cluster. The event listeners for data change
fire on each.

This is necessary for the creation event, as regions are only created
locally. The `RegionFactory.create()` event gives the current server
process the ability to host data content, so has to run on all servers
individually so that a new region exists on all of them.

## Gateway interaction
The metadata region is gateway enabled, meaning all region events (creation, update and deletion)
are replicated to all connected clusters. As the region listener is defined on all clusters,
the delivery of the region event for metadata results in the same operation to create, update
or delete the dynamic region being executed on all.

That is, regions are created on all clusters with a single command as the command action, the
metadata event, is passed to all clusters immediately or when they come online. This enables
regions to be created on all clusters when available as opposed to running the command across
all clusters which could fail due to firewalls or if some clusters are powered down.

### Dynamic region creation and use
In all clusters regions must be created before they can be used obviously.

In the local cluster this is inevitable. 

In remote clusters, the gateway sequence must ensure that the region creation events that make new
regions are delivered before any data for those regions. Refer to the server configuration section
[here] (usage/server.md) for details.

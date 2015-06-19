# Known Issues

## Efficiency
Dynamic region management does not restrict the number of regions that can be
created in a cluster.

From an operational perspective, each region has an administration overhead -- for
statistics, monitoring, etc. Beyond a certain number, the regions will operate more
efficiently in two clusters than one, but the logical relationship between regions
is not visible to Gemfire.

## Sub-regions / Namespaces
Sub-regions are not currently supported. Without such namespaces, all region names need
to be unique which can cause a clash for applications.

With sub-regions, all regions for an application could be in an unique namespace.

For example, "*wfn/employees*", "*wfn/organisations*".

Certain region names such as "*employees*" are likely to be required for many applications. 

## Naming restrictions
Gemfire places no restrictions on region naming, except that the region separator character `/`
cannot be used. Dynamic Region Management does not add further restrictions.

This allows names to be used which should perhaps be rejected as logical duplicates.
 
For example, both "*Customer List*", "*CustomerList*" and "*customerList*" could all exist.

At the very least this will cause confusion, and need escaping in OQL.

## Server groups
Dynamically created regions are allocated on all servers. Server groups are not currently
supported.

Server groups is a mechanism to specify that a region is only present on a selection of
servers.

This can be useful for small regions with multiple servers, where partitioning it would
distribute it in inefficiently small sections. 

It can also be useful to segregate the data of applications that cannot be co-hosted.

## Client attributes
Region metadata stores the attributes that are to be used to create the region on clients.

This does not allow these attributes to vary per client, which can affect behaviour.

A loader for the "*employees*" region would need this to be defined as `PROXY`,
whereas a browser for the _same_ region would need this to be defined as `CACHING_PROXY`.

## Gateway interaction
In order not to lose events, regions need to be created on remote clusters before data
can be added to them. 

As the current implementation does not get confirmation from the remote cluster that
the region has been correctly created, it needs to be configured on the *assumption*
that it will work.

A single delivery queue must be used, so that the region creation event has a chance
to complete (ie. the region is created) before the next event which may be data for
that new region. A single delivery queue allows for no parallelization, so it the
slowest gateway configuration option.

The region creation may fail on the remote cluster, meaning no remote region exists,
and data events delivered for that region will be discarded.

The region creation may also fail on the remote cluster if the region already exists,
meaning data events would be delivered into a different (non-empty) region than the
one intended.

## XML Validation
XML options are required to be consistent across all servers.

For example, the parameters specifying the datacenters names for global and regional
distribution via gateways should be consistent. Deviation would cause errors.

This needs either to be centralized to a single copy or validated, to eliminate the
chance for mis-configuration.

## "After Event" mechanism failures
Regions are created and destroyed by a two-step process. A function is used to
create, update or delete the metadata region entry. An after event listener
(`afterCreate()`, `afterUpdate()` or `afterDestroy()`) fires and the code in
this deals with the creation, update or deletion of the dynamic region.

Although this runs in the same thread as the metadata update, if the after event
operation fails the metadata update does not rollback. If such a problem occurs,
the metadata will mismatch with reality until the next cluster restart.

## "After Event" mechanism propagation
Region creation and deletion events are propagated from the servers to the
client via the normal Gemfire eventing mechanism. As this is an asynchronous
queue, there can be a temporal mismatch between client and servers while
messages are in the queue.

A region can be created on the servers, and not yet be known to a client
and therefore cannot be used.

A region can be deleted on the servers, and still be known to the client
briefly. If the client tries to use this region, the operation will fail.

## "After Region" mechanism failures
The `Region.clear()` operation on the metadata region would leave the
cache in an inconsistent state. 

The entries in the metadata region would be deleted, but their `afterDestroy()`
event would not be triggered, so the corresponding dynamic regions would
not be removed.

## Metadata distribution
Metadata must be distributed globally, and this results in all regions
specified in the metadata being created on all clusters.

If a region "*LOG_DC1*" is created intended to be local, the region "*LOG_DC1*"
will also be created on all clusters even though the content is not distributed.
This could cause confusion.

If a region "*EOD*" is created as REGIONAL for the North America region, it would
be created on all clusters although again data distribution would be not be to all.
This would stop the European clusters from creating a region "*EOD*" with
REGIONAL distribution as the region would already exist on those clusters. That
could be a problem if North America required the "*EOD*" region to be non-persistent
and Europe wanted it to be persistent.

## Indexes
Indexes cannot currently be specified when creating a region dynamically.

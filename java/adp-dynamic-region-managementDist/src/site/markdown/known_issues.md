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

## Gateway default
Region creation commands cannot specify the distribution policy

This would be useful to do,

```
{"server": { "distributionPolicy": ["REGIONAL"] } 
```

If the distribution policy choice *LOCAL*, *REGIONAL* or *GLOBAL* cannot be specified,
it will default to one of these and this will not suit all regions.

## Gateway sender
Region creation commands can specify the gateway names directly.

For example,

```
{"server": { "gatewaySenderIds": ["ds2"] } 
```

This is incorrect as the gateway names usually differ from cluster to cluster in a
network of clusters. For example, in cluster `ds1` the sender to cluster `ds2` is named `ds2`.
In cluster `ds2` the sender to cluster `ds1` is named `ds1`.

So, if the sender name is part of the region metadata, it will only work on some clusters,
those which have gateways with the specified name.

## XML Validation
XML options are required to be consistent across all servers.

For example, the parameters specifying the datacenters names for global and regional
distribution via gateways should be consistent. Deviation would cause errors.

This needs either to be centralized to a single copy or validated, to eliminate the
chance for mis-configuration.

# Usage : Native Client
A native client implementation of Dynamic Region Management is necessary to be
able to present a REST interaction mechanism via Node.js.

The implementation of Dynamic Region Management in the Native Client should
be functionally the same as any subsequent implemenation of a Java client.

## Metadata region
On the client the metadata region is not persistent, each client starts with this
region empty. This ensures that the client does not have stale metadata should
the client have been down for some time.

## Bootstrap
As part of a bootstrap process, a cache listener runs the `afterRegionLive()`
callback as the client initializes.

This does a `Region.get()` on each metadata entry, to retrieve the current
metadata into the client's cache. Then the `regionCreate()` process runs
in a similar fashion to the server side to create the region in the
client's cache.

### Client region attributes
The metadata stored on the servers includes the region attributes to be
used on the client ; this is the connection pool name `myPool` and the
region type of `PROXY` or `CACHING_PROXY`.

## Subscription-enabled pool
Although the bootstrap process creates all current dynamic regions on
the client, the client would not normally be aware of changes to the
metadata after it had started.

To override this, the client subscribes to changes on the metadata
region. 

When this changes on the server-side of the cluster, the changed
metadata entries are copied to the client's metadata region.

As these change, the cache listener events `afterCreate()` and
`afterDestroy()` are triggered, and this will create or delete
regions in the client's cache to match the change on the server
side.

## Node.js wrapped Functions
In the separate *node-gemfire* module, a generic function invocation
mechanism exists that allows REST calls to Node.js to be converted
into calls to the Native Client.

In practice, this means a REST call to Node.js with the appropriate
argument can trigger the Native Client to invoke the "*CreateRegion*"
or "*DestroyRegion*" function on the server-side.

If a region is created on the server-side cluster, the above subscription
mechanism results in the same region being created on the client's cache.
Once the region is in the client's cache, it can be used as a proxy
mechanism to read or write data to the server side caches.

The net effect here is "*CreateRegion*" can be run, and then the client
is able to access that region on the servers.


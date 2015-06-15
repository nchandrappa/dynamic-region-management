# Gateways
Titanium needs to support multiple connected clusters obviously, and in keeping with
the overall design goals for Dynamic Region Management this needs to be lightweight
in terms of the configuration required.

## Topologies
In terms of data distribution between clusters, three topologies are needed.

### Local
The most basic communication topology is none at all. That is, data which is
stored in the current cluster but not replicated to any of the other clusters.

This data is called "*LOCAL*".

### Global
The next communication topology is to distribute to everywhere. Data on the
current cluster is replicated to all other clusters.

This data is called "*GLOBAL*", reflecting the fact that the other clusters
wil be in datacenters in other countries across the world.

### Regional
Logically the last topology is partial distribution, where data on the current
cluster is replicated to a selection of other clusters.

Titanium places a restriction on this, where only one datacenter can be the
target. Of the possible partial sets of clusters, there is only one that is
valid.

This last topology is "*REGIONAL*", indicating that the other datacenter is in
the same geographic region. Ie. two datacenters in the same country.

## Metadata configuration
The Dynamic Region Management layers on top of standard Gemfire configuration
to implement remote distribution.

### Standard configuration
As part of standard Gemfire configuration to connect clusters, each cluster
is specifies a `gateway-sender`. This is a one-directional but possibly multiplexed
channel from the current cluster to one specific remote cluster.

Multiple `gateway-sender` sections connect this current cluster to multiple remote
clusters, and the counterpart set-up on these create two-directional communication.

## Metadata configuration
In the metadata configuration, in the "*cache.xml*" file, the specific gateways
to use for *REGIONAL* and *GLOBAL* distribution are listed. By definition, *LOCAL*
needs no gateways.

This enables a new region request to specify the required distribution policy
by reference (eg. "*REGIONAL*") rather than list the selected gateways in the
new region request.

## Default configuration
To facilitate lightweight configuration, a default configuration needs to be
specified.

This selects one of the three possibilies *GLOBAL*, *LOCAL* or *REGIONAL*
should be used if not directly specified in the new region request.

## New region request
A new region request can omit the distribution and allow this to default.
For example, the minimum to specify the server-side options is the region
type.

```
{ "server": { "type": "PARTITION" }"
```

Alternatively, the distribution policy can be specified.

```
{ "server": { "type": "PARTITION" ,
              "distributionPolicy": "REGIONAL" }"
```



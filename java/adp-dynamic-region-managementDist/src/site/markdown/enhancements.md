# Enhancements
Various simplifications and improvements are possible to Dynamic Region Management.

## Java client
A Java client is not currently implemented, only a Native client (C++) for
Node.js

This has not been a requirement to date, but would be an easy addition.

## "*cache.xml*" file

### XML
The required functions, "*CreateRegion*" and "*DestroyRegion*", could be added
via the `CacheInitializer` class, reducing the XML.

This would reduce the complexity of the XML file.

### Validation
Certain options are required, such as PDX must be configured with `read-serialized`
enabled.

The `CacheInitializer` could validate these have all been correctly configured
and log an appropriate error message if not. This would assist with prompt
detection of incorrect set-up.

## Environment specific configuration
The configuration held in the metadata region is for the current environment.

For example,

```
{ "local-max-memory" : "1024" }
```

If different environments have different tuning needs, multiple copies of the
metadata contents need to be maintained.

An alternative would be to hold environment specific overrides in the metadata
configuration.

For example,

```
{ "local-max-memory" : "1024" , "override-local-max-memory-for-DIT1" : "512" }
```

In this way, the metadata content would not need to differ from environment to
environment reducing the opportunity for configuration mismatches. The system
could select from the normal or from any override.

## Disk stores
The tested configuration uses the same disk store for persistent gateways, PDX,
the metadata region and the dynamically created regions.

It may be possible to use different diskstores, and this could be useful, for
example, to clear gateway queues in test environments.

This is untested.

## Cluster Configuration Service
Gemfire releases from 8.0 onwards provide a "*Cluster Configuration Service*".
[Click here for details] (http://gemfire.docs.pivotal.io/latest/userguide/deploying/gfsh/gfsh_persist.html)

This duplicates much of the functionality of the Dynamic Region Management service.

It is worth investigating if the Cluster Configuration Service can replace some or
all of the functionality of the Dynamic Region Management service, as this would
reduce the maintenance requirement on bespoke code.

## Gateway/security interaction
Gateway receivers are added via a cache initializer rather than in XML, to avoid a
deadline in the start-up sequence handshaking gateway receivers and senders when
security is present.

This is an issue on Gemfire 8.0, but is believed to be corrected on Gemfire 8.1.
Once validated, and assuming there are no other reasons to delay creating the
receiver (such as waiting for all regions to exist), the configuration could
revert to the simpler XML basis.

## Parsing
Some of the parameter handling is intolerant of whitespace, for example where
the distribution policies are specified in the "*cache.xml*" file.

## Region Options Validation
The `RegionOptionsValidator` class does not have access to the `DistributionPolicy`
within the `CreateRegion` function, so cannot validate the passed options for
distribution policy. This can allow invalid options which would see the region
creation fail although the metadata would be persisted.

## Automated tests
The amount of automated tests could be increased.

## Update Region mechanism
An outline implementation exists for changing the attributes of an existing
region.

This needs consideration if it is viable to allow regions to be modified
while running, and augment or remove the implementation of region update
accordingly.

There are very few options that can be changed while the cluster runs, so
it is unclear if there is any requirement to do so. Equally, changing the
options for the next restart without immediately changing the target region
may cause problems at restart, and would
mean the runtime configuration in the metadata region would differ from
the regions currently live.

## GFSH invocation mechanism
The arguments to `CreateRegion` and `DestroyRegion` are too complex to
be invoked from GFSH, which could be useful for maintenance.

`CreateRegion` could be extended to take the name of another region as
an argument, rather than specify the attributes. That is, to take two
`String` objects as arguments. The intention here is to create a new
region with the same attributes as an existing region.

`DestroyRegion` could be extended to tolerate a `String` as the argument,
so that it could be invoked from GFSH.
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
{ local-max-memory : 1024 }
```

If different environments have different tuning needs, multiple copies of the
metadata contents need to be maintained.

An alternative would be to hold environment specific overrides in the metadata
configuration.

For example,

```
{ local-max-memory : 1024 , local-max-memory-override-for-DIT1 : 512 }
```

In this way, the metadata content would not need to differ from environment to
environment reducing the opportunity for configuration mismatches. The system
could select from the normal or from any override.
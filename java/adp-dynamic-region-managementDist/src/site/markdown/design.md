# Design
The Dynamic Region Management service was designed by Pivotal Labs in response
to ADP's requirement, and they provided the initial implementation.

## Key features of the solution
Four key principles underly the Dynamic Region Management solution.

### Service based
The solution is provided as an additional runtime service built on top of Gemfire.

As this is a runtime service, new regions can be requested and old ones destroyed
by making service requests to the Gemfire cluster.

This bypasses the need to make changes to configuration files, code new Java
classes, or change any static content.

Gemfire is used to configure Gemfire directly, as opposed to indirectly using
configuration files. 

### Immediate and invisible
No partial or complete outage is required to adjust the configuration.

New regions are added to the system as it runs. No restart is required to
pick up changes configuration files or configuration classes as these
mechanisms are not used.

New regions are added immediately, and the lack of a bounce removes impact
and visibility to existing applications using the cluster.

### Web based
Region requests can be submitted via Node.js, a web-based mechanism.

This is simpler and therefore more reliable than coding a custom client
for region maintenance.

### JSON based
Region requests are made in JSON format, which is both a popular and
more intuitive format than XML, and with better browser support.

## Outline
The following outlines the solution. For more detail see the [implementation] (implementation.md).

### Client REST wrapper
#TODO invoke a function

### Region request format
#TODO some json goes here

### Metadata region
#TODO what it holds and why

### Event listeners
#TODO how these react to create regions on servers, or destroy

# TODO
```
TO DO
```

## Notes on Design

### Cluster Configuration Service
Gemfire releases from 8.0 onwards provide a "*Cluster Configuration Service*",
[Click here for details] (http://gemfire.docs.pivotal.io/latest/userguide/deploying/gfsh/gfsh_persist.html)

This provides similar functionality and could be considered as a replacement to
transition into place in the medium to longer term.
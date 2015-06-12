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
All interactions with Gemfire for Titanium use the "*node-gemfire*" module, which provides
an HTTP based interface rather than requiring specialized Gemfire knowledge.

One of the operations this supports is a "*CreateRegion*" command, which takes a region
name as an argument. This results in the region being created immediately, being available
for use, and being permanently stored in the configuration so that it will continue to
exist nomatter how many times the cluster is restarted. 

A counterpart "*DestroyRegion*" command is provide to remove a region should it no longer
be required. To avoid clashes with the other applications that don't use Dynamic Region Management,
the region deletion mechanism can only be used to delete regions that Dynamic Region
Management originally created.

### Security
As Dynamic Region Management is implemented as a layer on top of standard Gemfire,
no security can be bypassed.

If security is enabled, any attempt to create, delete or access a region will be
rejected if the wrong username/password combination is provided or if the username
does not have the necessary access rights.

### Two-step approach
A two-step approach is taken to Dynamic Region Management, using a specification stage
to drive a creation stage.

#### Specification step - Metadata region
The first step uses a hidden system region, named '__regionAttributesMetadata', to
hold a permanent record of the dynamic regions that should exist and their
configuration.

Addition or removal of entries in this metadata region triggers the second
step.

#### Creation step - Event listeners
Attached to the hidden system region holding the metadata is a cache listener.

The cache listener is triggered by changes to the metadata. When an entry
is added, the `afterCreate()` method is called in the cache listener and
this responds by creating the region in the cluster with the attributes
specified in the metadata.

As this second step is triggered by the first step automatically the
requirement (the metadata) and the actuality (the regions) should stay
matching.

#### Immediacy
Implicit in this two-step approach is immediacy. As soon as regions are
described in the metadata region they are created as real regions in
the cluster. Similarly, when regions are removed from the metadata
the corresponding real regions are removed from the cluster.

This is different from changing a "*cache.xml*" file. Although the
"*cache.xml*" file is essentially a metadata record, it is only
read at start-up time even if it subsequently changes.

### Region request format
The format chosen for region creation requests is the format stored
in the metadata region. Using one format throughout simplifies the
handling.

This format tries to hide as much Gemfire detail from the outside world,
and is held as a JSON object.

Each of the Gemfire attribues become JSON strings. For example,

```
{ "local-max-memory":"512" ,
  "recovery-delay":"-1" ,
  "startup-recovery-delay":"30" }
```

## Notes on Design

### Cluster Configuration Service
Gemfire releases from 8.0 onwards provide a "*Cluster Configuration Service*",
[Click here for details] (http://gemfire.docs.pivotal.io/latest/userguide/deploying/gfsh/gfsh_persist.html)

This provides similar functionality and could be considered as a replacement to
transition into place in the medium to longer term.
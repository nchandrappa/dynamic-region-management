# Objectives

## Background
In Gemfire, storage regions are normally defined using an XML configuration file,
or through the programmatic API.

An XML example,
```
<region name="banana">
  <region-attributes refid="PARTITION"/>
</region>	
```

A Java example using the API,
```
RegionFactory regionFactory = CacheFactory.getAnyInstance().createRegionFactory(RegionShortcut.PARTITION);
Region region = regionFactory.create("banana");
```

Both of these examples create a region named "*banana*" with the type of *PARTITION* (to
indicate the storage is sharded into parts on the available servers).

## Motivation
ADP's requirement for the Titanium platform is for the ability to be able to create
regions on demand, with minimum intervention.

Use of XML or the API requires the change to be implemented by the server-side team on
behalf of the client-side team. This does not fit with the rapid turnaround model of
Agile, with embedded experts.

## Notes on Objectives

### Titanium
Although Dynamic Region Management was created for the Titanium platform, it does not
need to be restricted to Titanium clusters and can be used elsewhere.

### Speed vs. Knowledge vs. Control
Dynamic Region Management enables regions to be created more quickly than raising
a request ticket to server-side support teams.

Responsibility for correct selection of region attributes and control processes
moves from the server-side support teams to the client-side teams.
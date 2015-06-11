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


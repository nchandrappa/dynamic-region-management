# Usage : Native Client

# TODO
```
TODO
```

## Subscription-enabled pool

## Metadata region

## REST wrapped Functions


6. Similarly, the `create` event is fired on the metadata region JS object running on each NodeJS client.
 7. A NodeJS callback picks up the `create` event and creates and configures the client version of the region.
 
 JS re-initialization
1. A NodeJS client boots.
2. A function is called that requests all existing metadata entries.
3. For each entry, the client-side region is created, thus backfilling any dynamic regions created before it had booted.
4. The JS client registers interest in the metadata region so that any new regions created server-side will fire the `create` event.




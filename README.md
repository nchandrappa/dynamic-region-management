# GemFire Dynamic Region Management
reference implementation for NodeJS

## Rough overview of creating a new region
 1. Call the JS function to create a region.
 2. The JS function sends the relevant parameters up to one of the GemFire servers, which runs the CreateRegion Java function
 3. The server adds an entry to the metadata region, which should be set up as persistent and replicated across the entire cluster. **TODO: mark the metadata region as persistent in the reference implementation**
 4. The entry is propagated to each server through GemFire replication.
 5. An `afterCreate` event is fired on each server. A listener picks up this event and does the actual creation and configuration of the region.
 6. Similarly, the `create` event is fired on the metadata region JS object running on each NodeJS client.
 7. A NodeJS callback picks up the `create` event and creates and configures the client version of the region.
 
## Java re-initialization
 1. A Java GemFire server boots.
 2. The `afterRegionCreate` callback fires on the metadata region after it is created via XML configuration.
 3. This callback iterates over each metadata region and recreates the region, thus backfilling any dynamic regions created before it had booted.

## JS re-initialization
1. A NodeJS client boots.
2. A function is called that requests all existing metadata entries.
3. For each entry, the client-side region is created, thus backfilling any dynamic regions created before it had booted.
4. The JS client registers interest in the metadata region so that any new regions created server-side will fire the `create` event.


## How to recompile Jar

1. `vagrant up && vagrant ssh`
2. `cd /vagrant && grunt`
3. This runs the grunt task `java:build`, which generates the jar file in /vagrant/java/build/lib/java.jar which can then be renamed and copied elsewhere to distribute


### Troubleshooting

#### Provisioning VM: Failed to mount folders in Linux guest...

Problem:

    Failed to mount folders in Linux guest. This is usually because
    the "vboxsf" file system is not available. Please verify that
    the guest additions are properly installed in the guest and
    can work properly. The command attempted was:
    
    mount -t vboxsf -o uid=id -u vagrant,gid=getent group vagrant | cut -d: -f3 vagrant /vagrant
    mount -t vboxsf -o uid=id -u vagrant,gid=id -g vagrant vagrant /vagrant

Solution:

1. vagrant ssh
2. sudo yum -y update && sudo /etc/init.d/vboxadd setup && exit
3. vagrant reload

For more info see: http://stackoverflow.com/questions/22717428/vagrant-error-failed-to-mount-folders-in-linux-guest


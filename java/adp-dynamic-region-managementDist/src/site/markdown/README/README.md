#Release Notes 0.3.4#
Addresses undesirable interaction with security framework that results
in a deadlock during startup when security, gateway senders and dynamic
region management are all present.

__You must make the following configuration changes in conjunction with this release__
* add `--disable-default-server` to the cache server startup options
* All gateway senders declared in cache.xml must be configured for "manual start"  (example below)

    ```xml
        <gateway-sender id="REMOTE" parallel="false" 
            remote-distributed-system-id="${REMOTE_DISTRIBUTED_SYSTEM_ID}" enable-batch-conflation="true" 
            enable-persistence="true" disk-store-name="GATEWAY_DISK_STORE" maximum-queue-memory="10" manual-start="true" />
    ```

* _the gateway receiver must be removed completely_
* The following "-Ds" must be defined in the cache server start script (example below)

    ```
    --J=-DGATEWAY_RECEIVER_START_PORT=5001
	--J=-DGATEWAY_RECEIVER_END_PORT=5009
    --J=-DCACHE_SERVER_PORT=112233
    ```

* The `--server-port` option should be removed from the startup script if present
* The following must be added to the bottom of cache.xml, beneath the function service

    ```xml
    <initializer>
        <class-name>io.pivotal.adp_dynamic_region_management.CacheInitializer</class-name>
		<parameter name="cacheServerPort">
			<string>${CACHE_SERVER_PORT}</string>
		</parameter>
	</initializer>
    ```



## Architecture
### Rough overview of creating a new region
 1. Call the JS function to create a region.
 2. The JS function sends the relevant parameters up to one of the GemFire servers, which runs the CreateRegion Java function
 3. The server adds an entry to the metadata region, which should be set up as persistent and replicated across the entire cluster. **TODO: mark the metadata region as persistent in the reference implementation**
 4. The entry is propagated to each server through GemFire replication.
 5. An `afterCreate` event is fired on each server. A listener picks up this event and does the actual creation and configuration of the region.
 6. Similarly, the `create` event is fired on the metadata region JS object running on each NodeJS client.
 7. A NodeJS callback picks up the `create` event and creates and configures the client version of the region.
 
### Java re-initialization
 1. A Java GemFire server boots.
 2. The `afterRegionCreate` callback fires on the metadata region after it is created via XML configuration.
 3. This callback iterates over each metadata region and recreates the region, thus backfilling any dynamic regions created before it had booted.

### JS re-initialization
1. A NodeJS client boots.
2. A function is called that requests all existing metadata entries.
3. For each entry, the client-side region is created, thus backfilling any dynamic regions created before it had booted.
4. The JS client registers interest in the metadata region so that any new regions created server-side will fire the `create` event.


## Development

### Prerequisites

* [Vagrant 1.6.x or later](http://www.vagrantup.com/)

### Quickstart

#### 1. Start the VM

    $ vagrant up

#### 2. Provision the VM

First, copy the following dependencies to your `adp-dynamic-region-management/tmp` directory:

* GemFire for 64 bit Linux RPM (example: `pivotal-gemfire-8.0.0.3-49715.el6.noarch.rpm`)
* GemFire C++ Native Client for 64 bit Linux Zip (example: `Pivotal_GemFire_NativeClient_Linux_64bit_8001_b6212.zip`)
* Node Gemfire Tarball (example: `node-gemfire-0.0.15.tar.gz`)

Then you should be able to install any needed dependencies into the VM with:

    $ vagrant provision

**Note**: If you don't have access to the exact versions of the above that are expected by our install scripts, the files you need to modify to install other versions are:

 * `bin/vagrant_build_project.sh`
 * `bin/vagrant_setup_centos6.5.sh`

#### 3. Use the VM

Your `adp-dynamic-region-management` directory is mounted inside your VM as `/vagrant`. You can make edits on either your workstation or inside the VM.

    $ vagrant ssh
    $ cd /vagrant

#### 4. Build and Run Tests

From inside the VM:

    $ grunt

**Note**: On your first run—before the JVM has warmed up—or on slow machines, certain tests may time out. If tests are failing due to `Error: Timeout - Async callback was not invoked within timeout specified by jasmine.DEFAULT_TIMEOUT_INTERVAL`, you should run them again, or increase `jasmine.DEFAULT_TIMEOUT_INTERVAL` in the failing test.

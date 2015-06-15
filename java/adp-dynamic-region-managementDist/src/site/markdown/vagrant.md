# Testing with Vagrant

__NOTE: These are the original instructions. Need to validate these
are still correct, and clarify if necessary.__

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

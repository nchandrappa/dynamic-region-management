const childProcess = require("child_process");
const async = require("async");

const cache = require("../../lib/cache");
const main = require("../../lib/main");
const regionCreator = require("../../lib/regionCreator");

require("../helpers/features.js");

function gfsh(command, callback) {
  childProcess.exec('gfsh -e "connect" -e "' + command + '"', callback);
}

feature("Dynamic region creation", function() {
  var originalDefaultTimeoutInterval;

  beforeEach(function() {
    originalDefaultTimeoutInterval = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 20000;
  });

  afterEach(function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalDefaultTimeoutInterval;
  });

  beforeEach(function(done) {
    main.init(done);
  });

  scenario("Calling a java function creates a region on all Java servers", function(done) {
    const newRegionName = "newRegion" + Date.now();

    async.series([

      // show that region does not exist on server1
      function(next) {
        gfsh('list regions --member=server1', function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).not.toMatch(newRegionName);
          next(error);
        });
      },

      // show that region does not exist on server2
      function(next) {
        gfsh("list regions --member=server2", function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).not.toMatch(newRegionName);
          next(error);
        });
      },

      // insert into regionMetadata
      function(next) {
        const regionOptions = {
          client: {
            type: "PROXY"
          }
        };

        regionCreator.createRegion(newRegionName, regionOptions, next);
      },

      // show that region exists on server1
      function(next) {
        gfsh('list regions --member=server1', function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).toMatch(newRegionName);
          next();
        });
      },

      // show that region exists on server2
      function(next) {
        gfsh('list regions --member=server2', function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).toMatch(newRegionName);
          next();
        });
      },

    ], function(error) {
      if(error) { fail(error); }
      done();
    });

  });

  scenario("Node client passes a region shortcut for the newly created region to use", function(done){
    const newRegionName = "newPartitionRegion" + Date.now();

    async.series([

      // show that region does not exist on server1
      function(next) {
        gfsh('list regions --member=server1', function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).not.toMatch(newRegionName);
          next(error);
        });
      },

      // show that region does not exist on server2
      function(next) {
        gfsh("list regions --member=server2", function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).not.toMatch(newRegionName);
          next(error);
        });
      },

      // create region
      function(next) {
        const regionOptions = {
          server: {
            type: "PARTITION"
          },
          client: {
            type: "PROXY"
          }
        };

        regionCreator.createRegion(newRegionName, regionOptions, next);
      },

      // show that region exists on server1
      function(next) {
        gfsh('list regions --member=server1', function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).toMatch(newRegionName);
          next();
        });
      },

      // show that region exists on server2
      function(next) {
        gfsh('list regions --member=server2', function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).toMatch(newRegionName);
          next();
        });
      },

      // show that region is PARTITION, not NORMAL
      function(next) {
        gfsh('describe region --name=' + newRegionName, function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).toMatch(/Data Policy.*partition/);
          next();
        });
      }

    ], function(error) {
      if(error) { fail(error); }
      done();
    });
  });

  // TODO: Add test coverage for gatewaySenderIds once we figure out how to stand up a gateway sender
  // to test against.
  xscenario("Node client passes gatewaySenderIds for the newly created region to use");

  // TODO: Add test coverage for asyncEventQueueIds once we figure out how to stand up an async event
  // queue to test against.
  xscenario("Node client passes asyncEventQueueIds for the newly created region to use");

  // TODO: Add test coverage for diskStoreName once we figure out how to check what disk store is being
  // used for a given region.
  xscenario("Node client passes diskStoreName for the newly created region to use");

  // TODO: Add test coverage for diskSynchronous once we figure out how to check whether the region is
  //  used for a given region.
  xscenario("Node client passes diskSynchronous for the newly created region to use");

  function createAndDescribeRegion(newRegionName, regionOptions, callback) {
    async.series([

      // show that region does not exist on server1
      function(next) {
        gfsh('list regions --member=server1', function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).not.toMatch(newRegionName);
          next(error);
        });
      },

      // show that region does not exist on server2
      function(next) {
        gfsh("list regions --member=server2", function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).not.toMatch(newRegionName);
          next(error);
        });
      },

      // create region
      function(next) {
        regionCreator.createRegion(newRegionName, regionOptions, next);
      },

      // show that region exists on server1
      function(next) {
        gfsh('list regions --member=server1', function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).toMatch(newRegionName);
          next();
        });
      },

      // show that region exists on server2
      function(next) {
        gfsh('list regions --member=server2', function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).toMatch(newRegionName);
          next();
        });
      },

      // show that region is PARTITION, not NORMAL
      function(next) {
        gfsh('describe region --name=' + newRegionName, function(error, stdout, stderr) {
          if(error) { fail(error); }
          next(null, stdout);
        });
      }

    ], callback);
  }

  scenario("Node client passes region attributes for the newly created region to use", function(done){
    const newRegionName = "newCustomizedServerRegion" + Date.now();

    const regionOptions = {
      server: {
        concurrencyLevel: 10,
        dataPolicy: "PERSISTENT_REPLICATE",
        enableAsyncConflation: true,
        enableSubscriptionConflation: true,
        asyncEventQueueIds: ["queue1", "queue2"],
        ignoreJTA: true,
        indexUpdateType: "asynchronous",
        initialCapacity: 100,
        isLockGrantor: true,
        loadFactor: 0.5,
        multicastEnabled: true,
        scope: "GLOBAL",
        diskStoreName: "myDiskStore",
        diskSynchronous: false,
        statisticsEnabled: true,
        cloningEnabled: true,
      },
      client: {
        type: "PROXY"
      }
    };

    createAndDescribeRegion(newRegionName, regionOptions, function(error, stdout) {
      if(error) {
        fail(error);
      } else {
        expect(stdout).toMatch(/concurrency-level.*10/);
        expect(stdout).toMatch(/Data Policy.*persistent replicate/);
        expect(stdout).toMatch(/enable-async-conflation.*true/);
        expect(stdout).toMatch(/enable-subscription-conflation.*true/);
        expect(stdout).toMatch(/ignore-jta.*true/);
        expect(stdout).toMatch(/index-maintenance-synchronous.*false/);
        expect(stdout).toMatch(/initial-capacity.*100/);
        expect(stdout).toMatch(/is-lock-grantor.*true/); // also tells us scope is global
        expect(stdout).toMatch(/load-factor.*0.5/);
        expect(stdout).toMatch(/multicast-enabled.*true/);
        expect(stdout).toMatch(/statistics-enabled.*true/);
        expect(stdout).toMatch(/cloning-enabled.*true/);
      }

      done();
    });
  });

  scenario("Node client configures concurrencyChecksEnabled", function(done){
    const newRegionName = "newCustomizedServerRegion" + Date.now();

    const regionOptions = {
      server: {
        concurrencyChecksEnabled: false
      },
      client: {
        type: "PROXY"
      }
    };

    createAndDescribeRegion(newRegionName, regionOptions, function(error, stdout) {
      if(error) { fail(error); }
      expect(stdout).toMatch(/concurrency-checks-enabled.*false/);
      done();
    });
  });
});

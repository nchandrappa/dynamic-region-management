const childProcess = require("child_process");
const async = require("async");

const cache = require("../../lib/cache");
const main = require("../../lib/main");

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
        cache
          .executeFunction("CreateRegion", [newRegionName, {}])
            .on("error", function(error) { fail(error); })
            .on("end", next);
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

      // insert into regionMetadata
      function(next) {
        const regionMetadata = {
          server: {
            type: "PARTITION"
          }
        };

        cache
          .executeFunction("CreateRegion", [newRegionName, regionMetadata])
            .on("error", function(error) { fail(error); })
            .on("end", next);
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
});

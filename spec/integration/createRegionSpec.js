const childProcess = require("child_process");
const async = require("async");

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

  scenario("Calling a java function creates a region on all Java servers", function(done) {
    const newRegionName = "newRegion" + Date.now();

    async.series([

      // start by destroying region if present
      function(next) {
        gfsh('destroy region --name=' + newRegionName, function(error, stdout, stderr) {
          expect(error).toBeFalsy();
          next(error);
        });
      },

      // show that region does not exist on server1
      function(next) {
        gfsh('list regions --member=server1', function(error, stdout, stderr) {
          expect(error).toBeFalsy();
          expect(stdout).not.toMatch(newRegionName);
          next(error);
        });
      },

      // show that region does not exist on server2
      function(next) {
        gfsh("list regions --member=server2", function(error, stdout, stderr) {
          expect(error).toBeFalsy();
          expect(stdout).not.toMatch(newRegionName);
          next(error);
        });
      },

      // insert into regionMetadata
      function(next) {
        const cache = getCache();

        cache
          .executeFunction("CreateRegionFunction", [newRegionName, {}])
            .on("error", function(error) { throw error; })
            .on("end", next);
      },

      // show that region exists on server1
      function(next) {
        gfsh('list regions --member=server1', function(error, stdout, stderr) {
          expect(error).toBeFalsy();
          expect(stdout).toMatch(newRegionName);
          next();
        });
      },

      // show that region exists on server2
      function(next) {
        gfsh('list regions --member=server2', function(error, stdout, stderr) {
          expect(error).toBeFalsy();
          expect(stdout).toMatch(newRegionName);
          next();
        });
      },

    ], function(error) {
      if(error) { throw error; }
      done();
    });

  });
});

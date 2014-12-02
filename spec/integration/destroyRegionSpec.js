const childProcess = require("child_process");
const async = require("async");

const cache = require("../../lib/cache");
const main = require("../../lib/main");
const regionCreator = require("../../lib/regionCreator");
const regionDestroyer = require("../../lib/regionDestroyer");

require("../helpers/features.js");

function gfsh(command, callback) {
  childProcess.exec('gfsh -e "connect" -e "' + command + '"', callback);
}

feature("Dynamic region destruction", function() {
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

  scenario("Calling a java function destroys a region on all Java servers", function(done) {
    const newRegionName = "newRegion" + Date.now();

    async.series([

      // create the region
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

      // destroy the region
      function(next) {
        regionDestroyer.destroyRegion(newRegionName, next);
      },

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
      }

    ], function(error) {
      if(error) { fail(error); }
      done();
    });

  });
});

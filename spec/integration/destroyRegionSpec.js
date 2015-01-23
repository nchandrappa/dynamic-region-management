const childProcess = require("child_process");
const async = require("async");

const main = require("../../lib/main");
const cache = require('../../lib/cache');
const regionCreator = require("../../lib/regionCreator");
const regionDestroyer = require("../../lib/regionDestroyer");
const metadataRegion = cache.getRegion("__regionAttributesMetadata");

require("../helpers/features.js");
const gfsh = require("../helpers/gfsh");

feature("Dynamic region destruction", function() {
  var originalDefaultTimeoutInterval;

  beforeEach(function() {
    originalDefaultTimeoutInterval = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 100000;
  });

  afterEach(function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalDefaultTimeoutInterval;
  });

  beforeEach(function(done) {
    async.series([
      function(next) { metadataRegion.clear(next); },
      function(next) { main.init(next); }
    ], done);
  });

  scenario("Calling a java function destroys a region on all Java servers", function(done) {
    const newRegionName = "newRegion" + Date.now();

    async.series([

      // create the region
      function(next) {
        const regionOptions = {
          client: {
            type: "PROXY",
            poolName: "myPool"
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

      // show that region exists on NodeJS client
      function(next) {
        expect(cache.getRegion(newRegionName)).toBeDefined();
        next();
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
      },

      // show that region does not exist on NodeJS client
      function(next) {
        expect(cache.getRegion(newRegionName)).not.toBeDefined();
        next();
      }

    ], function(error) {
      if(error) { fail(error); }
      done();
    });

  });
});

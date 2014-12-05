const async = require("async");

const main = require("../../lib/main");
const cache = require('../../lib/cache');

const regionCreator = require("../../lib/regionCreator");
const regionUpdater = require("../../lib/regionUpdater");

require("../helpers/features");
const gfsh = require("../helpers/gfsh");

feature("Update a regions attributes", function() {
  beforeEach(function() {
    originalDefaultTimeoutInterval = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
  });

  afterEach(function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalDefaultTimeoutInterval;
  });

  beforeEach(function(done) {
    main.init(done);
  });

  scenario("Updating a regions cloning enabled", function(done) {
    const newRegionName = "newRegion" + Date.now();

    async.series([

      function(next) {
        const regionOptions = {
          client: {
            type: "PROXY"
          }
        };

        regionCreator.createRegion(newRegionName, regionOptions, next);
      },

      function(next) {
        gfsh('list regions --member=server1', function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).toMatch(newRegionName);
          next();
        });
      },

      function(next) {
        gfsh('list regions --member=server2', function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).toMatch(newRegionName);
          next();
        });
      },

      function(next) {
        const region = cache.getRegion(newRegionName);
        expect(region).toBeDefined();

        regionUpdater.update(newRegionName, {
          server: {
            cloningEnabled: true
          }
        }, next);
      },

      function(next) {
        gfsh('describe region --name=' + newRegionName, function(error, stdout, stderr) {
          if(error) { fail(error); }
          expect(stdout).not.toMatch(/Non-Default Attributes Specific To The Hosting Members/);
          expect(stdout).toMatch(/cloning-enabled.*true/);
          next();
        });
      },

      function(next) {
        const metadataRegion = cache.getRegion("__regionAttributesMetadata");
        metadataRegion.get(newRegionName, function(error, regionOptions) {
          if(error) {
            next(error);
          } else {
            expect(regionOptions.server.cloningEnabled).toBe(true);
            next();
          }
        });
      }

    ], function(error) {
      if(error) { fail(error); }
      done();
    });
  });
});

const async = require("async");
const execFile = require("child_process").execFile;

const main = require("../../lib/main");
const cache = require("../../lib/cache");
const regionCreator = require("../../lib/regionCreator");
const metadataRegion = cache.getRegion("__regionAttributesMetadata");

require("../helpers/features.js");

feature("Dynamic region creation in the client", function(){
  beforeEach(function(done) {
    metadataRegion.clear(done);
  });

  scenario("Creating a region makes it available in the current NodeJS client", function(done) {
    const newRegionName = "newClientRegion" + Date.now();

    async.series([
      function(next) {
        main.init(next);
      },
      function(next) {
        const regionOptions = {
          client: {
            type: "PROXY",
            poolName: "myPool"
          }
        };
        regionCreator.createRegion(newRegionName, regionOptions, next);
      },
      function(next) {
        const region = cache.getRegion(newRegionName);
        expect(region).toBeDefined();
        next();
      },
    ], function(error) {
      if(error) { fail(error); }
      done();
    });

  });

  scenario("Creating a region marked as PROXY makes it available in the current NodeJS client", function(done) {
    const newRegionName = "newClientProxyRegion" + Date.now();

    async.series([
      function(next) {
        main.init(next);
      },
      function(next) {
        const regionOptions = {
          server: {
            type: "PARTITION"
          },
          client: {
            type: "PROXY",
            poolName: "myPool"
          }
        };

        regionCreator.createRegion(newRegionName, regionOptions, next);
      },
      function(next) {
        const region = cache.getRegion(newRegionName);
        expect(region.attributes.cachingEnabled).toBe(false);
        next();
      },
    ], function(error) {
      if(error) { fail(error); }
      done();
    });

  });

  scenario("Creating a region in another NodeJS client makes it available in the currently running NodeJS client", function(done) {
    const newRegionName = "newClientRegion" + Date.now();

    function createRegionInAnotherNodeProcess(next) {
      execFile("node", ["spec/scripts/createRegion.js", newRegionName], function(error, stdout, stderr) {
        if(error) { throw(error); }
        next();
      });
    }

    async.series([
      function(next) {
        main.init(next);
      },
      function(next) {
        createRegionInAnotherNodeProcess(next);
      },
      function(next) {
        const region = cache.getRegion(newRegionName);
        expect(region).toBeDefined();
        next();
      },
    ], function(error) {
      if(error) { fail(error); }
      done();
    });

  });

});

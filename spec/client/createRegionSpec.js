const async = require("async");

const main = require("../../lib/main");
const cache = require("../../lib/cache");
const regionCreator = require("../../lib/regionCreator");

require("../helpers/features.js");

feature("Dynamic region creation in the client", function(){
  scenario("Creating a region makes it available in the current NodeJS client", function(done) {
    const newRegionName = "newClientRegion" + Date.now();

    async.series([
      function(next) {
        main.init(next);
      },
      function(next) {
        const regionOptions = {
          client: {
            type: "PROXY"
          }
        }
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
            type: "PROXY"
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
});

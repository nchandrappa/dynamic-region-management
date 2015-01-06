const async = require("async");
const execFile = require('child_process').execFile;

const main = require("../../lib/main");
const cache = require("../../lib/cache");
const regionCreator = require("../../lib/regionCreator");
const metadataRegion = cache.getRegion("__regionAttributesMetadata");

require("../helpers/features.js");

function listRegions(callback) {
  execFile('node', ['./spec/scripts/listRegions.js'], function (error, stdout, stderr) {
    if(error) { callback(error); }
    const regions = stdout.trim().split("\n");
    callback(null, regions);
  });
}

describe("main", function() {
  beforeEach(function(done) {
    metadataRegion.clear(done);
  });

  describe("init", function() {
    it("creates any regions that are already present", function(done) {
      const regionName = "alreadyPresentRegion" + Date.now();

      async.series([
        function(next) {
          const regionOptions = {
            server: { type: "REPLICATE" },
            client: { type: "PROXY", poolName: "myPool" }
          };
          regionCreator.createRegion(regionName, regionOptions, next);
        },
        function(next) {
          // The call to main.init() inside listRegions should re-create and
          // list the newly-created region in that separate process
          listRegions(function(error, regions) {
            if(error) { fail(error); }
            expect(regions).toContain(regionName);
            next();
          });
        }
      ], function(error){
        if(error) { fail(error); }
        done();
      });
    });
  });
});

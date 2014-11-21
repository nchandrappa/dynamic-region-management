const async = require("async");
const execFile = require('child_process').execFile;

const main = require("../../lib/main");
const cache = require("../../lib/cache");
const regionCreator = require("../../lib/regionCreator");

require("../helpers/features.js");

function listRegions(callback) {
  execFile('node', ['./lib/listRegions.js'], function (error, stdout, stderr) {
    if(error) { callback(error); }
    const regions = stdout.trim().split("\n");
    callback(null, regions);
  });
}

describe("main", function() {
  describe("init", function() {
    it("creates any regions that are already present", function(done) {
      const regionName = "alreadyPresentRegion" + Date.now();

      async.series([
        function(next) { main.init(next); },
        function(next) {
          const regionOptions = {
            server: { type: "REPLICATE" },
            client: { type: "PROXY" }
          };
          regionCreator.createRegion(regionName, regionOptions, next);
        },
        function(next) {
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

const async = require("async");

const main = require("../../lib/main");
const cache = require("../../lib/cache");

feature("Dynamic region creation in the client", function(){
  scenario("Creating a region makes it available in the current NodeJS client", function(done) {
    const newRegionName = "newClientRegion" + Date.now();

    async.series([
      function(next) {
        main.init(next);
      },
      function(next) {
        cache
          .executeFunction("CreateRegion", [newRegionName, {}])
            .on("error", fail)
            .on("end", next);
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

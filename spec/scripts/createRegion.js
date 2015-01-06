const regionCreator = require("../../lib/regionCreator");

const regionName = process.argv[2];

const regionOptions = {
  client: {
    type: "PROXY",
    poolName: "myPool"
  }
};

regionCreator.createRegion(regionName, regionOptions, function(error) {
  if(error) { throw error; }
});

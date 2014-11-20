const cache = require("./cache");

var alreadyInitialized = false;

exports.init = function init(done) {
  if(!alreadyInitialized) {
    setUpRegionListener();
    alreadyInitialized = true;
  }

  setImmediate(done);
};

function setUpRegionListener() {
  const region = cache.getRegion("__regionAttributesMetadata");

  region.on("create", function onCreate(event) {
    const regionName = event.key;
    const regionOptions = event.newValue;
    cache.createRegion(regionName, regionOptions.client);
  });

  region.registerAllKeys();
}

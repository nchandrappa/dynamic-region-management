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

  region.on("create", function(event) {
    const regionName = event.key;
    cache.createRegion(regionName);
  });

  region.registerAllKeys();
}

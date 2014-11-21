const _ = require("lodash");
const async = require("async");

const cache = require("./cache");
const metadataRegion = cache.getRegion("__regionAttributesMetadata");

var alreadyInitialized = false;

exports.init = function init(done) {
  if(alreadyInitialized) {
    setImmediate(done);
  } else {
    async.series([setUpRegionListener, createPreexistingRegions], function(error) {
      if(error) { throw error; }
      alreadyInitialized = true;
      setImmediate(done);
    });
  }
};

function setUpRegionListener(done) {
  metadataRegion.on("create", function onCreate(event) {
    const regionName = event.key;
    const regionOptions = event.newValue;
    cache.createRegion(regionName, regionOptions.client);
  });

  metadataRegion.registerAllKeys();

  setImmediate(done);
}

function createPreexistingRegions(done) {
  metadataRegion.keys(function(error, keys) {
    if(error) { done(error); }
    metadataRegion.getAll(keys, function(error, values) {
      if(error) { done(error); }
      _.each(values, function(regionOptions, regionName) {
        cache.createRegion(regionName, regionOptions.client);
      });
      done();
    });
  });
}

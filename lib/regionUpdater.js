const cache = require("./cache");

exports.update = function update(regionName, regionOptions, callback) {
  var payload;
  var callbackError;

  function data(returnValue) { payload = returnValue; }
  function error(errorObject) { callbackError = callback(errorObject); }
  function end() { callback(callbackError, payload); }

  cache.executeFunction("UpdateRegion", [regionName, regionOptions])
    .on("data", data)
    .on("error", error)
    .on("end", end);
};

const cache = require("./cache");

exports.destroyRegion = function destroyRegion(regionName, callback){
  var payload;
  var callbackError;

  function data(returnValue) { payload = returnValue; }
  function error(errorObject) { callbackError = callback(errorObject); }
  function end() { callback(callbackError, payload); }

  cache.executeFunction("DestroyRegion", [regionName])
    .on("data", data)
    .on("error", error)
    .on("end", end);
};

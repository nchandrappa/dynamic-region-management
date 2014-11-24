const cache = require("./cache");

exports.createRegion = function createRegion(name, options, callback) {
  if(!callback) { throw "callback required."; }

  var errorToPassAlong;

  cache
    .executeFunction("CreateRegion", {
      arguments: [name, options],
      pool: "defaultPool"
    })
      .on("error", function(error){
        errorToPassAlong = error;
      })
      .on("end", function(){
        callback(errorToPassAlong);
      });
};

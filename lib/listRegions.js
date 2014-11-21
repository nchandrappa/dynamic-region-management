const _ = require('lodash');

const cache = require("./cache");
const main = require("./main");

main.init(function(){
  _.each(cache.rootRegions(), function(region){
    console.log(region.name);
  });
});

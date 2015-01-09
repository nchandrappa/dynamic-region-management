const _ = require('lodash');

const cache = require("../../lib/cache");
const main = require("../../lib/main");

main.init(function(){
  _.each(cache.rootRegions(), function(region){
    console.log(region.name);
  });
});

const Cache = require("gemfire").Cache;
const cache = new Cache("config/client.xml");

global.getCache = function getCache() {
  return cache;
};

const Cache = require("gemfire").Cache;
const cache = new Cache("config/client.xml");
module.exports = cache;

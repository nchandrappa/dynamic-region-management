const gemfire = require("gemfire");
gemfire.configure("config/client.xml");
const cache = gemfire.getCache();
module.exports = cache;

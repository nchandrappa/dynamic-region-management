const childProcess = require("child_process");

module.exports = function gfsh(command, callback) {
  childProcess.exec('gfsh -e "connect" -e "' + command + '"', callback);
};

const childProcess = require("child_process");

module.exports = function gfsh(command, callback) {
  childProcess.exec('gfsh -e "connect --locator=10.0.2.15[10334]" -e "' + command + '"', callback);
};

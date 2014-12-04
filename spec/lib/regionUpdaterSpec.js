const RegionUpdater = require("../../lib/regionUpdater");
const cache = require("../../lib/cache");

describe("regionUpdater", function() {
  describe(".update", function() {
    var regionName;
    var regionOptions;

    beforeEach(function() {
      regionName = "newRegion" + Date.now();
      regionOptions = { foo: "bar" };
    });

    it("calls the UpdateRegion function with the region name and the new settings object", function() {
      function onFunction(name, callback) {
        return this;
      }

      const executeFunctionEventEmitter = { on: onFunction };
      spyOn(cache, "executeFunction").and.returnValue(executeFunctionEventEmitter);

      RegionUpdater.update(regionName, regionOptions, function(){});

      expect(cache.executeFunction).toHaveBeenCalledWith("UpdateRegion", [regionName, regionOptions]);
    });

    describe('when the DestroyRegion function reports a success', function() {
      it("passes the function's result to the callback", function(done) {
        const functionReturnValue = {"return": "value"};

        function onFunction(name, callback) {
          if(name === "data") {
            callback(functionReturnValue);
          }

          if(name === "end") {
            callback();
          }

          return this;
        }

        const executeFunctionEventEmitter = { on: onFunction };
        spyOn(cache, "executeFunction").and.returnValue(executeFunctionEventEmitter);

        RegionUpdater.update(regionName, regionOptions, function(error, result){
          if(error) { fail(error); }
          expect(result).toBe(functionReturnValue);
          done();
        });
      });
    });

    describe('when the destroy region function returns an error', function() {
      it("passes the error to the callback", function(done) {
        const regionName = "newRegion" + Date.now();
        const errorFromFunctionExecution = new Error();

        function onFunction(name, callback) {
          if(name === "error") {
            callback(errorFromFunctionExecution);
          }
          return this;
        }

        const executeFunctionEventEmitter = { on: onFunction };
        spyOn(cache, "executeFunction").and.returnValue(executeFunctionEventEmitter);

        RegionUpdater.update(regionName, regionOptions, function(error){
          expect(error).toBe(errorFromFunctionExecution);
          done();
        });
      });
    });
  });
});

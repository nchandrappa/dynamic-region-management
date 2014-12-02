const RegionDestroyer = require("../../lib/regionDestroyer");
const cache = require("../../lib/cache");

describe("RegionDestroyer", function() {
  describe(".destroy", function() {
    it("calls the DestroyRegion function with the region name", function() {
      const regionName = "newRegion" + Date.now();

      function onFunction(name, callback) {
        return this;
      }

      const executeFunctionEventEmitter = { on: onFunction };
      spyOn(cache, "executeFunction").and.returnValue(executeFunctionEventEmitter);

      RegionDestroyer.destroyRegion(regionName, function(){});

      expect(cache.executeFunction).toHaveBeenCalledWith("DestroyRegion", [regionName]);
    });

    describe('when the DestroyRegion function reports a success', function() {
      it("passes the function's result to the callback", function(done) {
        const regionName = "newRegion" + Date.now();
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

        RegionDestroyer.destroyRegion(regionName, function(error, result){
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

        RegionDestroyer.destroyRegion(regionName, function(error){
          expect(error).toBe(errorFromFunctionExecution);
          done();
        });
      });
    });
  });
});

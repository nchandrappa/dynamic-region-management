const gemfire = require("gemfire");
gemfire.configure("config/wan-client.xml")

const cache = gemfire.getCache();

const metadataRegion = cache.getRegion("__regionAttributesMetadata");

const regionOptions = { client: { type: "PROXY"}, server: { type: "PARTITION_REDUNDANT_PERSISTENT" , diskStoreName: "myDiskStore"}};

cache.executeFunction("CreateRegion", {arguments:["my-first-region", regionOptions], pool: "myPool"}).on("error", function(error){
		console.log("ERROR: " + error);
	}).on("end", function(result){
		console.log("FINISHED");		
	});

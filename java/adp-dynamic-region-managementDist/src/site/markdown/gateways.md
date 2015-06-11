# TODO
```
TO DO
```
#Overview
When a new region is created via the dynamic region creation API, the gateway behavior 
will be controlled via configuration on the gemfire cache server.  The application will
 use the "distribution-policy" attribute of the region options.  The distribution policy 
may take on the values: GLOBAL, REGIONAL, LOCAL.

Note that if the "gateway-sender-ids"" region attribute is populated, it will be ignored 
by the dynamic region management code and a warning will be printed.  The distribution 
policy will still control.

If the distribution policy is not provided on the regin options, a default policy,
also configured on the cache server, will be used.

#Configuring the Server Side
The adp-dynamic-region-management jar must be 




# Notes
Many important things have been deferred because of the time line.
Here are some of them.
* Automated Tests
* Currently the job of validating region options that affect the gateway setup 
  is divided between the GatewaySenderIdsOption and the DistributionPolicy
  making DistributionPolicy an abstraction that does not really stand alone
* RegionOptionsValidator does not validate the distributionPolicy region option
  because the object with that responsibility, DistributionPolicy, is not available
  within the CreateRegion Function.  If an invalid DistributionPolicy is passed,
  the region entry will still be inserted into the region metadata but the 
  actual region creation will fail.
* In BasicDistributionPolicy , no attempt has been made to handle any whitespace in 
  the property values
* Nothing checks consistency of distribution policy among the cache servers.



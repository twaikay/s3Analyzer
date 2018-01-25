# Overview

A simple S3 Analysis Tool with the following features:

* Fast scan all S3 buckets in an Amazon account and returns the following info:
	+ Bucket name
	+ Creation date (of the bucket)
	+ Number of files
	+ Total size of files
	+ Last modified date (most recent file of a bucket)
	+ Bucket size (bytes, KB, MB, GB, TB)
	+ Group by regions, storage type
* Supports search filter.
* Accepts JSON request and returns JSON response.
* Can be triggered by the following:
	+ Command line using AWS CLI
	+ Restful API (HTTP POST of JSON request).

# Program

* Packaged as a Lambda application with the usual benefits (i.e. concurrent requests, easy deployment for both CLI/microservice etc).

* Uses multiple worker threads in a fixed thread pool for concurrent processing.

* Each bucket is allocated a worker thread which:
	+ allocates a sub thread continuously querying S3 API (AWS max fetch size limit: 1000)  
	+ concurrently processing the objects info returned by the S3 API.

* Idempotent. Multiple internal AWS lambda triggers and concurrent requests will not impact the outcome of the response. 

## Requirements

* Build: 
	* `Java JDK 8`
	* `Maven 3.x`

Unit testing is done locally using [localstack](https://github.com/localstack/localstack).

## Installation

Build the application. It is packaged as a Lambda application and the output will be a single jar file (`aws_waiKay-1.0.jar`).

Create the function in AWS with the following parameters:

	runtime=java8
	function-name=handleRequest
	handler=com.waikay.aws.s3.S3Lambda::handleRequest
	zip-file fileb://${yourFolder}/aws_waiKay-1.0.jar
	environment Variables={springConfig=applicationContext.xml} 

## Request messages

Sample JSON request and response messages are in the `test/` folder:

* `listAllObjectMetas.js`: list all objects in all regions' buckets.
* `listAllObjectMetasPrefix.js`: includes search filter for listAllObjectMetas.js
* `listBucketsByStorageClass.js`: list all objects in all regions' buckets, group by storage class.
* `listBucketsByStorageClassPrefix.js`: includes search filter for listBucketsByStorageClass.js
* `listBuckets.js`: list all bucket names
* `listAllObjectMetasWithBucket.ps`: list all objects in a bucket

## Run

* Using Command line:

	Format: 
	>aws lambda invoke --function-name handleRequest `outputfile` --payload file://`yourPayloadFilePath`

	Example: 
	>aws lambda invoke --function-name handleRequest `listBucketsByStorageClass.log` --payload file://`/home/ubuntu/aws/lambda/test/listAllObjectMetasPrefix.js`

	You can run the following script which will spawn all request files in the folder at the same time. The response files will be in the same source folder. 

	Format:
	>~/aws/bin/utils.sh runAll `yourSourceRequestsFolder`
	
	Example:
	>~/aws/bin/utils.sh runAll ~/aws/lambda

* RESTful API:

	The public URL is: [https://cxfjsg4pad.execute-api.us-east-1.amazonaws.com](https://cxfjsg4pad.execute-api.us-east-1.amazonaws.com)

	Perform a HTTP POST of the sample JSON request messages in the `test/request/` folder. There are a lot of tools which can perform this task (e.g. SOAPUI, Android apps, curl etc). 

	Sample `curl` command as follows:

	>curl -H "Content-Type: application/json" -X POST -d '{"method": "listAllObjectMetas", "prefix": "wkw101/1/x1"}' https://cxfjsg4pad.execute-api.us-east-1.amazonaws.com/prod/s3test

## Limitations

While the program strives hard to perform as fast as possible, depending on the no. of S3 objects/buckets,  requests like `listAllObjectMetas` or `listBucketsByStorageClass` could run beyond max time limits of the following interfaces:
 
* CLI: Lambda functions: max run time 5 minutes per request. 

* API gateway: max run time 30 seconds per request. 

**Workaround for the time being:**

* Send a `listBuckets.js` request to get a list of buckets across all regions.
* Spawn multiple concurrent `listAllObjectMetasWithBucket.ps` requests on individual buckets. 
* It is advisable to provide a fine grained search prefix to speed up the processing.  
* The client will have to aggregate the results eventually.

**Solution (future roadmap):**

While the original intention is to build a simple tool, a better enterprise solution could be catered in the future roadmap by having **asynchronous processing of requests** and hence all microservices will be completed within a short timeframe:

*  Modify the program core to:
	*  listen to SQS.
	*  implement the ElasticCache:
		+ write-through persistence to a DB.
		+ emission policy (remove stale cache entries after X period).
*  Deploy this program core as a continuously running application deployed in an EC2 instance.
*  Expose the same lambda function for accepting incoming requests, but now direct the request to the SQS.
*  For each request:
	*  If requestID exists in the request message (existing request), 
		+ sends a message to SQS to get the response.
	*  If requestID is not in the request message (new request), 
		+ create a request (with a unique requestID) and send to SQS. 
		+ Return the response immediately which contains this requestID and status=PENDING.
* When program consumes the message in SQS:
	*  If it is a new request, 
		+ return the response as status=Pending.
		+ reuse the processing logic here i.e. delegates to the worker threads to perform the operation. 
		+ Stores the results in the cache (key=requestID, value={resultsObject, status=COMPLETE}).
	*  If it is an existing request, 
		+ get the resultObject from cache based on requestID. 
			+ If status==PENDING, return the response.
			+ If status==COMPLETE, return the resultObject in the response and remove the cache entry.
* Client constantly polls the status of the request by providing the requestID.

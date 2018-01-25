package com.waikay.aws.s3.formatter;

import java.util.*;

import com.amazonaws.services.s3.model.*;

import com.waikay.aws.common.*;
import com.waikay.aws.s3.*;

public class S3JsonResponseFormatter {

//********************************************************************

	public static String createJsonRegionListMsg( Map<String,List<BucketVO>> regionBucketVOs ) {
		/* output:
		{ "regions": [ 
			{ "region": x,
		  	  "buckets": [ jsonBucketMsg ]
		  	}
		  ]
		}
		*/
		//output
		StringBuilder sb = new StringBuilder("{");
		sb.append("\"regions\": [");
		int mapSize = regionBucketVOs.size();
		int mapCount = 0;
		for (Map.Entry entry : regionBucketVOs.entrySet()) {
			String region = (String)entry.getKey();
			List<BucketVO> bucketVOs = (List<BucketVO>)entry.getValue();
			sb.append("{ \"region\": \"").append(region).append("\", ");
			sb.append("\"buckets\": [");
			int size = bucketVOs.size();
			int count = 0;
			for (BucketVO bucketVO : bucketVOs) {
				sb.append(createJsonBucketMsg(bucketVO));
				if (++count < size) {
					sb.append(",");
				}
			}
			sb.append("] }");
			if (++mapCount < mapSize) {
				sb.append(",");
			}
		}
		sb.append("] }");
		return sb.toString();
	}

//********************************************************************

	public static String createJsonBucketMsg( BucketVO bucketVO ) {
		/* output:
		{ "bucket" : {
		  	"name": x,
			"creationDate": x,
		  	"totalFiles": x,
		  	"totalFilesSize": [ "xx bytes", "xx KB", "xx MB", .. ],
		  	"latestFileModDate": x
		  	}
		}
		*/

		//total file sizes
		StringBuilder sizeSb = new StringBuilder();
		Map<String,String> multiSizes = Util.multiSizeFormats(bucketVO.getTotalFilesSize());
		int size = multiSizes.size();
		int count = 0;
		for (Map.Entry entry : multiSizes.entrySet()) {
			String type = (String)entry.getKey();
			String value = (String)entry.getValue();
			sizeSb.append("\"").append(value).append(" ").append(type).append("\"");
			if (++count < size) {
				sizeSb.append(",");
			}
		}

		//output
		StringBuilder sb = new StringBuilder("{");
		sb.append("\"bucket\": {")
			.append("\"name\": \"").append(bucketVO.getBucket().getName()).append("\", ")
			.append("\"creationDate\": \"").append(bucketVO.getBucket().getCreationDate()).append("\", ")
			.append("\"totalFiles\": \"").append(bucketVO.getTotalFiles()).append("\", ")
			.append("\"totalFilesSize\": [").append(sizeSb.toString()).append("], ")
			.append("\"latestFileModDate\": \"").append(bucketVO.getLatestFileModDate()).append("\", ")
			.append("\"latestFile\": \"").append(bucketVO.getLatestFilename()).append("\"")
		.append("} }");

		return sb.toString();
	}

//********************************************************************

	public static String createJsonStorageBucketMsg( Map<String,Map<StorageClassKeyVO,BucketVO>> storageClassMap ) {
		/* output:
		{ "storageClass": [
			{ "storageType": x,
			  "details": [ jsonBucketMsg ]
			}
		  ]
		}
		*/

		//output
		StringBuilder sb = new StringBuilder("{ \"storageClass\": [");
		int mapCount = 0;
		int mapSize = storageClassMap.size();
		for (Map.Entry entry : storageClassMap.entrySet()) {
			String type = (String)entry.getKey();
			sb.append("{ \"storageType\": \"").append(type).append("\", ");
			sb.append("\"details\": [");
			Map<StorageClassKeyVO,BucketVO> storageMap = (Map<StorageClassKeyVO,BucketVO>)entry.getValue();
			int count = 0;
			int size = storageMap.size();
			for (Map.Entry entry2 : storageMap.entrySet()) {
				StorageClassKeyVO key = (StorageClassKeyVO)entry2.getKey();
				BucketVO storageBucketVO = (BucketVO)entry2.getValue();
//debugL("key:" + key + "! storageBucketVO:" + storageBucketVO + "!");
				String jsonRegionMsg = createJsonBucketMsg(storageBucketVO);
				sb.append(jsonRegionMsg);
				if (++count < size) {
					sb.append(",");
				}
			}
			sb.append("] }");
			if (++mapCount < mapSize) {
				sb.append(",");
			}
		}
		sb.append("] }");

		return sb.toString();
	}

//********************************************************************

	public static String createJsonBucketListMsg( List<Bucket> buckets ) {
		/* output:
		{ "buckets": [ "x1", "x2", ... ]
		}
		*/

		//output
		StringBuilder sb = new StringBuilder("{ \"buckets\": [");
		int mapCount = 0;
		int mapSize = buckets.size();
		for (Bucket bucket : buckets) {
			sb.append("\"").append(bucket.getName()).append("\"");
			if (++mapCount < mapSize) {
				sb.append(",");
			}
		}
		sb.append("] }");

		return sb.toString();
	}
	
//********************************************************************

	private static void debugL( Object o ) {
		String threadName = Thread.currentThread().getName() + " - " + Thread.currentThread().getId();
//!!    	_log.info("--S3ObjectListThread: " + threadName + ": " + o);
    	System.out.println("--S3JsonResponseFormatter: " + threadName + ": " + o);
	}
}

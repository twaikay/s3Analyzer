package com.waikay.aws.s3;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;

import com.waikay.aws.common.*;

public class S3ObjectListThread extends S3ProcessorThread {

	private static final Logger _log = LogManager.getLogger(S3ObjectListThread.class);
	public static final int STATUS_DONE = 0;
	public static final int STATUS_FAIL = 1;

	private List<S3ObjectSummary> objectSummaries_ = new ArrayList<S3ObjectSummary>();
	private int status_ = -1;
	private String errorMsg_ = null;
	private ObjectListing listing_ = null;

	@Autowired
	Constants constants;
	
//********************************************************************

	public Object call() {
		Object retObj = null;
		ListObjectsV2Result result = null;
		ListObjectVO listObjectVO = (ListObjectVO)params_[0];
		BucketVO bucketVO = (BucketVO)params_[1];
		String region = bucketVO.getRegion();
		String debugPrefix = "call: " + region + ": " + bucket_;
debugL(debugPrefix + ": start:");

		try {
			ListObjectsV2Request request = new ListObjectsV2Request()
				.withBucketName(bucket_)
				.withMaxKeys(listObjectVO.getFetchSize());
			request.setPrefix(listObjectVO.getPrefix());
			request.setDelimiter(listObjectVO.getDelimiter());
debugL(debugPrefix + ": listObjectVO:" + listObjectVO + "!");

			int maxRetries = constants.MAX_ACCESS_RETRIES;
			String status = null;
			do {
				for (int retryCount=1; retryCount<=maxRetries; retryCount++) { 
					try {
						result = client_.listObjectsV2(request);
						status = null;
						break;
					} catch (Exception e) {
						status = e.getMessage();
debugL(debugPrefix + ": retryCount:" + retryCount + "! maxRetries:" + maxRetries + "! excep: " + status);
						try { Thread.sleep(50); } catch (Exception e1) { }
					}
				}
				if (status != null) {
					status_ = STATUS_FAIL;
					errorMsg_ = status;
					return retObj;
				}
				String token = result.getNextContinuationToken();
//debugL("call: token:" + token + "!");	
				request.setContinuationToken(token);
				add2ObjectSummaries(result);

				try { Thread.sleep(100); } catch (Exception e) { }
			} while (result.isTruncated());
			status_ = STATUS_DONE;

		} catch (Exception e) {
debugL(debugPrefix + ": excep:" + Util.getStackTrace(e));
			status_ = STATUS_FAIL;
//			throw e;
		}
debugL(debugPrefix + ": end");			
		return retObj;
	}

//********************************************************************

	private void add2ObjectSummaries( ListObjectsV2Result result ) {
		synchronized (objectSummaries_) {
			List<S3ObjectSummary> objectSummaries = result.getObjectSummaries();
//debugL("add2ObjectSummaries: objectSummaries.size:" + objectSummaries.size() + "!");
			objectSummaries_.addAll(objectSummaries);
		}		
	}
	
//********************************************************************

	public List<S3ObjectSummary> consumeObjectSummaries() {
		List<S3ObjectSummary> list = null;
		synchronized (objectSummaries_) {
			list = new ArrayList<S3ObjectSummary>(objectSummaries_);
			objectSummaries_.clear();
		}
		return list;	
	}

//********************************************************************

	public int getObjectSummariesSize() {
		synchronized (objectSummaries_) {
			return objectSummaries_.size();
		}
	}

//********************************************************************

	public int getStatus() {
		return status_;
	}

	public String getErrorMsg() {
		return errorMsg_;
	}

//*********************************************************
	
	private void debugL( Object o ) {
		String threadName = Thread.currentThread().getName() + " - " + Thread.currentThread().getId();
    	_log.info(threadName + ": " + o);
//    	System.out.println("--S3ObjectListThread: " + threadName + ": " + o);
	}
}

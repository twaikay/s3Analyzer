package com.waikay.aws.common;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.beanutils.MethodUtils;

import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.auth.*;
import com.amazonaws.client.builder.*;

public class Util {

	private static final Logger _log = LogManager.getLogger(Util.class);

//********************************************************************

	public static AmazonS3 getS3Client() {
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
			.withForceGlobalBucketAccessEnabled(true);
		builder.setPathStyleAccessEnabled(true);
		return builder.build();
	}

//////////////////////////////////////	
	
	public static AmazonS3 getS3Client( String endpointURL, String region ) {
		AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpointURL, region);
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
			.withEndpointConfiguration(config);
/*
//			.withRegion(region)
			.withCredentials(new AWSStaticCredentialsProvider(
				new BasicAWSCredentials(awsAccessKey_, awsSecretKey_)
				)
			);
*/			
		builder.setPathStyleAccessEnabled(true);
		return builder.build();
	}

//********************************************************************

	public static String getStackTrace( Exception e ) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

//********************************************************************

	public static Map<String,String> multiSizeFormats( long size ) {
		Map<String,String> retMap = new LinkedHashMap<String,String>();
		retMap.put(Constants.SIZE_BYTES, size+"");
		float kbSizeVal = size / 1024;
		retMap.put(Constants.SIZE_KB, String.format("%.2f", kbSizeVal));

		float mbSizeVal = kbSizeVal / 1024;
		retMap.put(Constants.SIZE_MB, String.format("%.2f", mbSizeVal));
		
		float gbSizeVal = mbSizeVal / 1024;
		retMap.put(Constants.SIZE_GB, String.format("%.2f", gbSizeVal));

		float tbSizeVal = gbSizeVal / 1024;
		retMap.put(Constants.SIZE_TB, String.format("%.2f", tbSizeVal));

		return retMap;
	}

//********************************************************************

	public static int getIntValue( String value, int defaultValue ) {
		int retVal = defaultValue;
		if (value!=null && !"".equals(value)) {
			try {
				retVal = Integer.parseInt(value);
			} catch (Exception e) {
debugL("getIntegerValue: excep:" + getStackTrace(e));
			}
		}
		return retVal;
	}

//*******************************************************

	public static Object invokeMethod( Object sourceObj, String mtdName, Object... objs )
		throws Exception {
		return MethodUtils.invokeMethod(sourceObj, mtdName, objs);
	}

//*********************************************************
	
	private static void debugL( Object o ) {
		String threadName = Thread.currentThread().getName() + " - " + Thread.currentThread().getId();
    	_log.info(threadName + ": " + o);
//    	System.out.println("--S3Lambda: " + threadName + ": " + o);
	}
}

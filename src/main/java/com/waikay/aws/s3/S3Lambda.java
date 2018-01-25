package com.waikay.aws.s3;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.model.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import com.waikay.aws.common.*;
import com.waikay.aws.s3.*;
import com.waikay.aws.s3.formatter.*;

public class S3Lambda implements RequestHandler<Map<String,Object>, JSONObject> {
	private static final Logger _log = LogManager.getLogger(S3Lambda.class);
	private AppRepository repo_ = AppRepository.getInstance();

//*********************************************************
	
	@Override
	public JSONObject handleRequest( Map<String,Object> inMap, Context context ) {
		String retStr = "{}";
		JSONParser parser = new JSONParser();
		String mtdName = (String)inMap.get("method");
		try {
debugL("repo:" + repo_ + "!");
			if (mtdName==null || "".equals(mtdName)) {
				retStr = createErrorMsg(mtdName, "method is required");
			} else {
debugL("mtdName:" + mtdName + "!");					
				//process	
				ListObjectVO listObjectVO = setupListObjectVO(inMap);
				S3Analyzer analyzer = (S3Analyzer)repo_.getBean("s3Analyzer");
				analyzer.init();
				retStr = (String)Util.invokeMethod(this, mtdName, inMap, listObjectVO, analyzer);
			}
		} catch (Exception e) {
			String err = Util.getStackTrace(e);
			retStr = createErrorMsg(mtdName, err);
debugL("excep: " + err + "!");
		}

		//return
		JSONObject json = null;
		try {
			json = (JSONObject)parser.parse(retStr);
		} catch (Exception e) {
			String err = Util.getStackTrace(e);
			retStr = createErrorMsg(mtdName, err);
			try { json = (JSONObject)parser.parse(retStr); } catch (Exception e1) { }
debugL("excep in parsing json: " + err + "!");
		}
debugL("json:" + json + "!");
debugL("end"); 
        return json;
	}

//*********************************************************
	
	public String listBuckets( Map<String,Object> inMap, ListObjectVO listObjectVO, S3Analyzer analyzer ) 
		throws Exception {
		List<Bucket> buckets = analyzer.listBuckets();
		String outStr = S3JsonResponseFormatter.createJsonBucketListMsg(buckets);
		return outStr;
	}

//*********************************************************
	
	public String listAllObjectMetas( Map<String,Object> inMap, ListObjectVO listObjectVO, S3Analyzer analyzer ) 
		throws Exception {
		Map<String,List<BucketVO>> map = analyzer.listAllObjectMetas(listObjectVO);
debugL("listAllObjectMetas: regionBucketVOs:" + map + "!");
		String outStr = S3JsonResponseFormatter.createJsonRegionListMsg(map);
debugL("listAllObjectMetas: outStr:" + outStr + "!");
		return outStr;
	}

//*********************************************************
	
	public String listAllObjectMetasWithBucket( Map<String,Object> inMap, ListObjectVO listObjectVO, S3Analyzer analyzer ) 
		throws Exception {
		String bucketName = (String)inMap.get("bucket");
		Map<String,List<BucketVO>> map = analyzer.listAllObjectMetasWithBucket(listObjectVO, bucketName);
debugL("listAllObjectMetasWithBucket: regionBucketVOs:" + map + "!");
		String outStr = S3JsonResponseFormatter.createJsonRegionListMsg(map);
debugL("listAllObjectMetasWithBucket: outStr:" + outStr + "!");
		return outStr;
	}

//*********************************************************
	
	public String listBucketsByStorageClass( Map<String,Object> inMap, ListObjectVO listObjectVO, S3Analyzer analyzer ) 
		throws Exception {
		Map<String,Map<StorageClassKeyVO,BucketVO>> map = analyzer.listBucketsByStorageClass(listObjectVO);
debugL("listBucketsByStorageClass: map:" + map + "!");
		String outStr = S3JsonResponseFormatter.createJsonStorageBucketMsg(map);
debugL("listBucketsByStorageClass: outStr:" + outStr + "!");
		return outStr;
	}

//*********************************************************

	private ListObjectVO setupListObjectVO( Map<String,Object> inMap ) {
		ListObjectVO listObjectVO = new ListObjectVO();

		//prefix, delim
		String prefix = (String)inMap.get("prefix");
		if (prefix!=null && !"".equals(prefix)) {
			listObjectVO.setPrefix(prefix);
			String delim = (String)inMap.get("delimiter");
			if (delim==null || "".equals(delim)) {
				delim = Constants.DEFAULT_FILE_DELIM;
			}		
			listObjectVO.setDelimiter(delim);
		}
debugL("prefix:" + prefix + "! listObjectVO:" + listObjectVO + "!");		

		int fetchSize = Util.getIntValue((String)inMap.get("fetchSize"), listObjectVO.getFetchSize());
		listObjectVO.setFetchSize(fetchSize);

		return listObjectVO;
	}

//*********************************************************
	
	private String createErrorMsg( String mtdName, String errMsg ) {
		StringBuilder sb = new StringBuilder();
		sb.append("{ \"method\": \"").append(mtdName).append("\", ")
			.append("\"error\": \"").append(errMsg).append("\" }");
		return sb.toString();
	}

//*********************************************************
	
	private void debugL( Object o ) {
		String threadName = Thread.currentThread().getName() + " - " + Thread.currentThread().getId();
    	_log.info(threadName + ": " + o);
//    	System.out.println("--S3Lambda: " + threadName + ": " + o);
	}
}

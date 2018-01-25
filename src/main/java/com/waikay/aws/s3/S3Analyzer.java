package com.waikay.aws.s3;

import java.util.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.auth.*;
import com.amazonaws.client.builder.*;

import com.waikay.aws.common.*;
import com.waikay.aws.s3.command.*;

public class S3Analyzer {

	private static final Logger _log = LogManager.getLogger(S3Analyzer.class);
	private AmazonS3Client client_ = null;
	@Value("#{T(java.lang.Integer).parseInt('${app_aws_s3_noOfThreads}')}")
	private int noOfThreads_;

//********************************************************************

	public void init() {
		if (client_ == null) {
        	client_ = (AmazonS3Client)Util.getS3Client();
		}
debugL("init: end");		
	}

	public void init( String endPoint, String region ) {
		if (client_ == null) {
        	client_ = (AmazonS3Client)Util.getS3Client(endPoint, region);
		}
debugL("init: end");		
	}

//********************************************************************

	public Map<String,List<BucketVO>> listAllObjectMetas() 
		throws Exception {
		return listAllObjectMetas(null);
	}

	public Map<String,List<BucketVO>> listAllObjectMetas( ListObjectVO listObjectVO ) 
		throws Exception {
		List<Command> commands = new ArrayList<Command>();
		commands.add(new ObjectSummaryCommand());
		return listAllObjectMetas(listObjectVO, commands);
	}

	public Map<String,List<BucketVO>> listAllObjectMetas( ListObjectVO listObjectVO, List<Command> commands ) 
		throws Exception {
		List<Bucket> buckets = listBuckets();
		return processBuckets(listObjectVO, commands, buckets);
	}

//********************************************************************

	public Map<String,List<BucketVO>> listAllObjectMetasWithBucket( ListObjectVO listObjectVO, String bucketName ) 
		throws Exception {
		List<Command> commands = new ArrayList<Command>();
		commands.add(new ObjectSummaryCommand());
		return listAllObjectMetasWithBucket(listObjectVO, bucketName, commands);
	}

	public Map<String,List<BucketVO>> listAllObjectMetasWithBucket( ListObjectVO listObjectVO, String bucketName, List<Command> commands )
		throws Exception {
		Bucket bucket = new Bucket(bucketName);
		List<Bucket> buckets = new ArrayList<Bucket>();
		buckets.add(bucket);
		return processBuckets(listObjectVO, commands, buckets);
	}

//********************************************************************

	public Map<String,List<BucketVO>> processBuckets( ListObjectVO listObjectVO, List<Command> commands, List<Bucket> buckets )
		throws Exception {
		Map<String,List<BucketVO>> regionBucketVOs = new HashMap<String,List<BucketVO>>();
		Set<Future> futureSet = new HashSet<Future>();
		ExecutorService threadPool = Executors.newFixedThreadPool(noOfThreads_);
		CompletionService svc = new ExecutorCompletionService(threadPool);
debugL("processBuckets: listObjectVO:" + listObjectVO + "!");
		try {
			AppRepository repo = AppRepository.getInstance();
			for (Bucket bucket : buckets) {
				BucketVO bucketVO = new BucketVO();
				bucketVO.setBucket(bucket);
				String bucketName = bucket.getName();
				String region = client_.getBucketLocation(bucketName);
				bucketVO.setRegion(region);
debugL("processBuckets: bucketName99:" + bucketName + "! region:" + region + "!");
				S3BucketProcessorThread processorThread = (S3BucketProcessorThread)repo.getBean("s3BucketProcessorThread");
debugL("processBuckets: processorThread:" + processorThread + "!");
				processorThread.setClient(client_);
				processorThread.setBucket(bucketName);
				processorThread.setParams(bucketVO, commands, listObjectVO, threadPool);
				futureSet.add(svc.submit(processorThread));
			}

debugL("processBuckets: next");
			while (!futureSet.isEmpty()) {
				Future future = svc.take();
				BucketVO bucketVO = (BucketVO)future.get();
				String regionStr = bucketVO.getRegion();
				List<BucketVO> bucketVOList = regionBucketVOs.get(regionStr);
				if (bucketVOList == null) {
					bucketVOList = new ArrayList<BucketVO>();
				}
				bucketVOList.add(bucketVO);
				regionBucketVOs.put(regionStr, bucketVOList);
				futureSet.remove(future);
			}
		} finally {
			//properly shut down
			try {
				threadPool.shutdownNow(); 
			} catch (Exception e) {
debugL("processBuckets: excep:" + Util.getStackTrace(e));		
			}
			try { 
//debugL("listAllObjectMetas: waiting for shutdown..");		
				threadPool.awaitTermination(10, TimeUnit.SECONDS);
//debugL("listAllObjectMetas: shutdown?");		
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

debugL("\n\n===========================================\n\n");
		for (Map.Entry entry : regionBucketVOs.entrySet()) {
			String region = (String)entry.getKey();
			List<BucketVO> bucketVOs = (List<BucketVO>)entry.getValue();
			for (BucketVO bucketVO : bucketVOs) {
debugL("listAllObjectMetas: final: region:" + region + ": bucketVO:" + bucketVO + "!");
			}
		}
		return regionBucketVOs;
	}

//********************************************************************

	public Map<String,Map<StorageClassKeyVO,BucketVO>> listBucketsByStorageClass( ListObjectVO listObjectVO ) 
		throws Exception {
//debugL("listBucketsByStorageClass:");	
		List<Command> commands = new ArrayList<Command>();
		commands.add(new ObjectSummaryCommand());
		commands.add(new GroupByObjectStorageClassCommand());
		Map<String,List<BucketVO>> regionBucketVOs = listAllObjectMetas(listObjectVO, commands);
//debugL("listBucketsByStorageClass: regionBucketVOs:" + regionBucketVOs + "!");	
		Map<String,Map<StorageClassKeyVO,BucketVO>> storageClassMap = new HashMap<String,Map<StorageClassKeyVO,BucketVO>>();
		for (Map.Entry entry : regionBucketVOs.entrySet()) {
			String region = (String)entry.getKey();
			List<BucketVO> bucketVOs = (List<BucketVO>)entry.getValue();
			for (BucketVO bucketVO : bucketVOs) {
//debugL("listBucketsByStorageClass: region:" + region + ": bucketVO:" + bucketVO + "!");	
				Map<String,Map<StorageClassKeyVO,BucketVO>> map = bucketVO.getStorageClassMap();
				for (Map.Entry entry2 : map.entrySet()) {
					String storageKey = (String)entry2.getKey();
					Map<StorageClassKeyVO,BucketVO> storageMap = (Map<StorageClassKeyVO,BucketVO>)entry2.getValue();
					Map<StorageClassKeyVO,BucketVO> storageClassMapValue = (Map<StorageClassKeyVO,BucketVO>)storageClassMap.get(storageKey);
					if (storageClassMapValue == null) {
						storageClassMap.put(storageKey, storageMap);
					} else {
						storageClassMapValue.putAll(storageMap);
						storageClassMap.put(storageKey, storageClassMapValue);
					}
				}
//debugL("listBucketsByStorageClass: region:" + region + ": storageClassMap:" + storageClassMap + "!");	
			}
		}
debugL("listBucketsByStorageClass: storageClassMap:" + storageClassMap + "!");	
		return storageClassMap;
	}

//********************************************************************

	public List<Bucket> listBuckets() 
		throws Exception {
//debugL("listBuckets:");	
		List<Bucket> buckets = client_.listBuckets();
		for (Bucket bucket : buckets) {

		}
		return buckets;
	}

//********************************************************************

	private void debugL( Object o ) {
		String threadName = Thread.currentThread().getName() + " - " + Thread.currentThread().getId();
    	_log.info(threadName + ": " + o);
//    	System.out.println("--S3Analyzer: " + threadName + ": " + o);
	}
}

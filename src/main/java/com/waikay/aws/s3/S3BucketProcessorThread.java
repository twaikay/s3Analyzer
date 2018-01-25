package com.waikay.aws.s3;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;

import com.waikay.aws.common.*;

public class S3BucketProcessorThread extends S3ProcessorThread {

	private static final Logger _log = LogManager.getLogger(S3BucketProcessorThread.class);
	
//********************************************************************

	public Object call() {
//debugL("call:");			
		BucketVO bucketVO = (BucketVO)params_[0];
		String region = bucketVO.getRegion();
		List<Command> commands = (List<Command>)params_[1];
		ListObjectVO listObjectVO = (ListObjectVO)params_[2];
		ExecutorService threadPool = (ExecutorService)params_[3];
		CompletionService svc = new ExecutorCompletionService(threadPool);
		String debugPrefix = "call: " + region + ": " + bucket_;

		try {
debugL(debugPrefix + ": listObjectVO:" + listObjectVO + "!");
			AppRepository repo = AppRepository.getInstance();
			String bucketName = bucketVO.getBucket().getName();
			long totalFilesCount = bucketVO.getTotalFiles();
			long totalFileSize = bucketVO.getTotalFilesSize();

			//start the objects list thread
			S3ObjectListThread listThread = (S3ObjectListThread)repo.getBean("s3ObjectListThread");
debugL(debugPrefix + ": listThread:" + listThread + "!");
			listThread.setClient(client_);
			listThread.setBucket(bucketName);
			listThread.setParams(listObjectVO, bucketVO);
//debugL(debugPrefix + ": starting listThread...");
			svc.submit(listThread);

			//process the objects
			List<S3ObjectSummary> objectSummaries = listThread.consumeObjectSummaries();
			totalFilesCount += objectSummaries.size();
			while (true) {
				//start the object processor thread
//debugL("------------------------------------------");				
				if (objectSummaries.size() > 0) {
					totalFilesCount += objectSummaries.size();
debugL(debugPrefix + ": processing: totalFilesCount:" + totalFilesCount + "! objectSummaries.size():" + objectSummaries.size() + "!");
					for (S3ObjectSummary objectSummary : objectSummaries) {
						for (Command command : commands) {
							bucketVO = (BucketVO)command.execute(bucketVO, objectSummary);
						}
					}
//debugL("listObjectMetas: S3ObjectProcessorThread: finish processing");
				}

				//subsequent batch
				objectSummaries = listThread.consumeObjectSummaries();
				if (objectSummaries.size() > 0) {
					continue;
				}

				//check is done
				int status = listThread.getStatus();
//debugL("listObjectMetas: " + bucketName + ": status: " + status);
				if (status == listThread.STATUS_DONE) {
					break;
				} else
				if (status == listThread.STATUS_FAIL) {
debugL("listObjectMetas: " + bucketName + ": exception: " + listThread.getErrorMsg());
					break;
				}
				try { Thread.sleep(1000); } catch (Exception e) { }
			}
debugL(debugPrefix + ": totalFilesCount:" + totalFilesCount + "! bucketVO:" + bucketVO + "!");

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
debugL(debugPrefix + ": excep:" + Util.getStackTrace(e));
//			throw e;
		}
debugL(debugPrefix + ": end");			
		return bucketVO;
	}

//*********************************************************
	
	private void debugL( Object o ) {
		String threadName = Thread.currentThread().getName() + " - " + Thread.currentThread().getId();
    	_log.info(threadName + ": " + o);
//    	System.out.println("--S3BucketProcessorThread: " + threadName + ": " + o);
	}
}

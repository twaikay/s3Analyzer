package com.waikay.aws.s3.command;

import java.util.*;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.waikay.aws.common.*;
import com.waikay.aws.s3.*;

public class ObjectSummaryCommand implements Command {

	public Object execute( Object... objs )
		throws Exception {
		BucketVO bucketVO = (BucketVO)objs[0];
		S3ObjectSummary objectSummary = (S3ObjectSummary)objs[1];

		baseProcessing(bucketVO, objectSummary);

		return bucketVO;
	}

//*********************************************************

	public void baseProcessing( BucketVO bucketVO, S3ObjectSummary objectSummary ) {
		//last modified date
		Date lastFileDate = bucketVO.getLatestFileModDate();
		Date currFileDate = objectSummary.getLastModified();
		if (lastFileDate==null || currFileDate.compareTo(lastFileDate) > 0) {
			bucketVO.setLatestFileModDate(currFileDate);
			bucketVO.setLatestFilename(objectSummary.getKey());
		}

		//file size
		long lastFileSize = bucketVO.getTotalFilesSize();
		long currFileSize = objectSummary.getSize();
		bucketVO.setTotalFilesSize(lastFileSize+currFileSize);

		//total files
		long totalFiles = bucketVO.getTotalFiles() + 1;
		bucketVO.setTotalFiles(totalFiles);

		//misc
		String storageClass = objectSummary.getStorageClass();
		bucketVO.setStorageClass(storageClass);
	} 
}

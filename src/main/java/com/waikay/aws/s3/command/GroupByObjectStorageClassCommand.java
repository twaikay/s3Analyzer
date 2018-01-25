package com.waikay.aws.s3.command;

import java.util.*;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.waikay.aws.common.*;
import com.waikay.aws.s3.*;

public class GroupByObjectStorageClassCommand extends ObjectSummaryCommand {

	public Object execute( Object... objs )
		throws Exception {
		BucketVO bucketVO = (BucketVO)objs[0];
		S3ObjectSummary objectSummary = (S3ObjectSummary)objs[1];

		String region = bucketVO.getRegion();
		String storageClass = objectSummary.getStorageClass();
		Map<String,Map<StorageClassKeyVO,BucketVO>> storageClassMap = bucketVO.getStorageClassMap();
		Map<StorageClassKeyVO,BucketVO> storageMap = storageClassMap.get(storageClass);
		if (storageMap == null) {
			storageMap = new HashMap<StorageClassKeyVO,BucketVO>();
		}
		StorageClassKeyVO key = new StorageClassKeyVO(region, bucketVO.getBucket().getName());
		BucketVO storageBucketVO = storageMap.get(key);
		if (storageBucketVO == null) {
			storageBucketVO = new BucketVO();
			storageBucketVO.setRegion(region);
			storageBucketVO.setBucket(bucketVO.getBucket());
		}
		baseProcessing(storageBucketVO, objectSummary);
		storageMap.put(key, storageBucketVO);
		storageClassMap.put(storageClass, storageMap);

		return bucketVO;
	}
}

package com.waikay.aws.s3;

import java.util.*;

import com.amazonaws.services.s3.model.*;
import com.waikay.aws.common.StandardToStringBuilder;

public class BucketVO {

	private String region_ = null;
	public void setRegion( String region ) {
		region_ = region;
	}
	public String getRegion() {
		return region_;
	}
		
	private Bucket bucket_ = null;
	public void setBucket( Bucket bucket ) {
		bucket_ = bucket;
	}
	public Bucket getBucket() {
		return bucket_;
	}

	private long totalFiles_ = 0;
	public void setTotalFiles( long totalFiles ) {
		totalFiles_ = totalFiles;
	}
	public long getTotalFiles() {
		return totalFiles_;
	}

	private long totalFilesSize_ = 0;
	public void setTotalFilesSize( long totalFilesSize ) {
		totalFilesSize_ = totalFilesSize;
	}
	public long getTotalFilesSize() {
		return totalFilesSize_;
	}

	private Date latestFileModDate_ = null;
	public void setLatestFileModDate( Date latestFileModDate ) {
		latestFileModDate_ = latestFileModDate;
	}
	public Date getLatestFileModDate() {
		return latestFileModDate_;
	}

	private String latestFilename_ = null;
	public void setLatestFilename( String latestFilename ) {
		latestFilename_ = latestFilename;
	}
	public String getLatestFilename() {
		return latestFilename_;
	}

	private String storageClass_ = null;
	public void setStorageClass( String storageClass ) {
		storageClass_ = storageClass;
	}
	public String getStorageClass() {
		return storageClass_;
	}

	private Map<String,Map<StorageClassKeyVO,BucketVO>> storageClassMap_ = new HashMap<String,Map<StorageClassKeyVO,BucketVO>>();
	public void setStorageClassMap( Map<String,Map<StorageClassKeyVO,BucketVO>> storageClassMap ) {
		storageClassMap_ = storageClassMap;
	}
	public Map<String,Map<StorageClassKeyVO,BucketVO>> getStorageClassMap() {
		return storageClassMap_;
	}
	
	public String toString() {
		return StandardToStringBuilder.toString(this);
	}
}

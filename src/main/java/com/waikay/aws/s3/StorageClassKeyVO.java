package com.waikay.aws.s3;

import java.util.*;

import com.amazonaws.services.s3.model.*;
import com.waikay.aws.common.StandardToStringBuilder;

public class StorageClassKeyVO {

	private String region_ = null;
	public void setRegion( String region ) {
		region_ = region;
	}
	public String getRegion() {
		return region_;
	}
		
	private String bucketName_ = null;
	public void setBucketName( String bucketName ) {
		bucketName_ = bucketName;
	}
	public String getBucketName() {
		return bucketName_;
	}

	public String toString() {
		return StandardToStringBuilder.toString(this);
	}

	public StorageClassKeyVO( String region, String bucketName ) {
		region_ = region;
		bucketName_ = bucketName;
	}

	@Override
	public int hashCode() {
		int regionHashCode = 0;
		if (region_ != null) {
			regionHashCode = region_.hashCode();
//System.out.println("region:" + region_ + "! hashcode:" + regionHashCode + "!");
		}
		int bucketNameHashCode = 0;
		if (bucketName_ != null) {
			bucketNameHashCode = bucketName_.hashCode();
//System.out.println("bucketName:" + bucketName_ + "! hashcode:" + bucketNameHashCode + "!");
		}
		return regionHashCode + bucketNameHashCode;
	}

	@Override
	public boolean equals( Object obj ) {
		StorageClassKeyVO key = (StorageClassKeyVO)obj;
		String region = key.getRegion();
		boolean validRegionFlg = false;
		if (region==region_ || (region!=null && region.equals(region_))) {
			validRegionFlg = true;
		}

		String bucketName = key.getBucketName();
		boolean bucketNameFlg = false;
		if (bucketName==bucketName_ || (bucketName!=null && bucketName.equals(bucketName_))) {
			bucketNameFlg = true;
		}

//System.out.println("equals: region:" + region + "! region_:" + region_ + "!");
//System.out.println("equals: bucketName:" + bucketName + "! bucketName_:" + bucketName + "!");
//System.out.println("equals: final:" + (validRegionFlg && bucketNameFlg));
		return (validRegionFlg && bucketNameFlg);

	}
}

package com.waikay.aws.s3;

import java.util.*;

import com.amazonaws.services.s3.model.*;
import com.waikay.aws.common.StandardToStringBuilder;

public class ListObjectVO {

	private String prefix_ = null;
	public void setPrefix( String prefix ) {
		prefix_ = prefix;
	}
	public String getPrefix() {
		return prefix_;
	}
		
	private String delimiter_ = null;
	public void setDelimiter( String delimiter ) {
		delimiter_ = delimiter;
	}
	public String getDelimiter() {
		return delimiter_;
	}

	private int fetchSize_ = 1000;
	public void setFetchSize( int fetchSize ) {
		fetchSize_ = fetchSize;
	}
	public int getFetchSize() {
		return fetchSize_;
	}

	public String toString() {
		return StandardToStringBuilder.toString(this);
	}
}


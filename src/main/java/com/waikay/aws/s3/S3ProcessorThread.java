package com.waikay.aws.s3;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import com.amazonaws.services.s3.AmazonS3Client;

public abstract class S3ProcessorThread implements Callable<Object> {

	protected AmazonS3Client client_ = null;
	protected Object[] params_ = null;
	protected String bucket_ = null;

//********************************************************************

	public void setClient( AmazonS3Client s ) {
		client_ = s;
	}
	public void setBucket( String s ) {
		bucket_ = s;
	}
	public void setParams( Object... s ) {
		params_ = s;
	}

//*********************************************************
}

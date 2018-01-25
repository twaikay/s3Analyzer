package com.waikay.aws.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Constants {

	@Value("#{T(java.lang.Integer).parseInt('${app_aws_s3_maxAccessRetries}')}")
	public int MAX_ACCESS_RETRIES = 3;

	public final static String SIZE_BYTES = "bytes";
	public final static String SIZE_KB = "KB";
	public final static String SIZE_MB = "MB";
	public final static String SIZE_GB = "GB";
	public final static String SIZE_TB = "TB";

	public final static String DEFAULT_FILE_DELIM = "/";

}

package com.waikay.aws.common;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringStyle;

public class StandardToStringBuilder {

	private static final ToStringStyle STYLE = new StandardToStringBuilder.StandardToStringStyle();

	public static String toString( Object object ) {
		return ReflectionToStringBuilder.toString(object, STYLE);
	}

	static class StandardToStringStyle extends ToStringStyle {
		private static final long serialVersionUID = 1L;

		StandardToStringStyle() {
			setUseShortClassName(true);
		}

		protected void appendFieldStart( StringBuffer buffer, String fieldName ) {
			if (fieldName != null) {
				buffer.append(StringUtils.stripEnd(fieldName, "_"));
				buffer.append(getFieldNameValueSeparator());
			}
		}
	}
}


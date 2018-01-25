package com.waikay.aws.common;

public interface Command {

	public Object execute( Object... objs )
		throws Exception;
}

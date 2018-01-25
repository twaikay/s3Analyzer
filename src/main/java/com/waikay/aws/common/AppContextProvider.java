package com.waikay.aws.common;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class AppContextProvider implements ApplicationContextAware {

	private ApplicationContext appCtx_;

	public ApplicationContext getAppContext() {
		return appCtx_;
	}

	public void setApplicationContext( ApplicationContext applicationContext ) 
		throws BeansException {
		appCtx_ = applicationContext;
	}
}

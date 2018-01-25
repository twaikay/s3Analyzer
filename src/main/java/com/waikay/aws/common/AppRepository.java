package com.waikay.aws.common;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.net.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.*;

public class AppRepository {
		
	private static final Logger _log = LogManager.getLogger(AppRepository.class);
	   
	private int runCount_ = 0;	
    private static final AppRepository _instance = new AppRepository();
	private Properties _prop = null;
	private AbstractXmlApplicationContext _ctx = null;

//********************************************************************
	
	private AppRepository() {
		//spring config			
debugL("AppRepository: start");
		try {	
			_ctx = new ClassPathXmlApplicationContext("/applicationContext.xml");
		} catch (Exception e) {
debugL("exception in loading spring config: " + e.getMessage());
			_log.error(e);
			throw e;
		}			
	}
	
//********************************************************************

	public static AppRepository getInstance() { 
		return _instance;
	}

//********************************************************************

    public Object getBean( String name ) { 
		return _ctx.getBean(name);
	}

    public AbstractXmlApplicationContext getContext() { 
		return _ctx;
	}

//********************************************************************
	
    public Properties getProp() { 
		return _prop;
	}

/////////////////////////////////////
	
	public String getProperty( String key ) {
		return _prop.getProperty(key);
	}

//********************************************************************
	
	private static void debugS( Object o ) {
		String threadName = Thread.currentThread().getName() + " - " + Thread.currentThread().getId();
    	_log.info(threadName + ": " + o);
//    	System.out.println("--AppRepository: " + threadName + ": " + o);		
	}
	private void debugL( Object o ) {
		String threadName = Thread.currentThread().getName() + " - " + Thread.currentThread().getId();
    	_log.info(threadName + ": " + o);
//    	System.out.println("--AppRepository: " + threadName + ": " + o);
	}
	
//********************************************************************
	
	
}



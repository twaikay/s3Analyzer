package com.waikay.aws.common;

import java.io.*;
import java.util.*;

import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class ResourceConfig extends PropertyPlaceholderConfigurer {

	private static Map<String, String> properties = new HashMap<String, String>();

//********************************************************************
	
	public static void setProperties( Map<String, String> properties ) {
		ResourceConfig.properties = properties;
	}

//********************************************************************
	
	public static Map<String, String> getAllProperties() {
		return properties;
	}
	
//********************************************************************
	
	public static String getProperty( final String name ) {
		return properties.get(name);
	}

///////////////////////////////////////

	public static String getProperty( ApplicationContext ctx, final String name ) {
		ClassPathXmlApplicationContext appCtx = (ClassPathXmlApplicationContext)ctx;
		ConfigurableListableBeanFactory bf = appCtx.getBeanFactory();
		String key = "${" + name + "}";
		String value = bf.resolveEmbeddedValue(key);
		if (key.equals(value)) {
			return null;
		}
		return value;
	}

//********************************************************************
	
	public static void setProperty( final String key, String value ) {
		properties.put(key, value);
	}

//********************************************************************
	
	@Override
	protected void loadProperties( final Properties props ) 
		throws IOException {
		super.loadProperties(props);
		for (final Map.Entry<Object, Object> entry : props.entrySet()) {
			properties.put((String) entry.getKey(), (String) entry.getValue());
		}
	}

//********************************************************************
	
}

package com.britesnow.snow.web;

import java.util.Map;

public interface PropertyPostProcessor {

	/**
	 * Allow an application to programmatically override the properties before the application initialization. 
	 * 
	 * @param properties the properties loaded
	 * @return the properties to be used for initialization of the application
	 */
	public Map processProperties(Map properties);
}

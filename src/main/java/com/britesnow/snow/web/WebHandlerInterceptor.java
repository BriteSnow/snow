package com.britesnow.snow.web;

import java.lang.reflect.Method;

public interface WebHandlerInterceptor {

	/**
	 * Called before any @WebModel, @WebAction 
	 * 
	 * @param method the method being called
	 * @param rc the requestContext
	 * @return true to tell the controller to call the method, false to byPass the method call. 
	 * 
	 * Note: The after method will always be called regardless of the before(..) return value. 
	 */
	public boolean before(Method method,RequestContext rc);
	
	/**
	 * Similar than before, but after.
	 * @param method
	 * @param rc
	 */
	public void after(Method method,RequestContext rc);
	
}

/* Copyright 2010 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web;

/**
 * 
 * A requestLifeCycle gets called when a request start and end. Any WebModule may have a RequestLifeCycle.
 * 
 * To configure a RequestLifeCycle class for a WebModule, add the following in the <em>configure()</em>: 
 * <pre>bind(RequestLifeCycle.class).to(MyRequestLifeCycle.class)</pre>
 * Obviously, the  @Provides would also work.
 * 
 */
public interface RequestLifecycle {

    /**
     * Get called after the Hibernate openSessionView and the Authentication process.
     * @param rc
     */
    public void start(RequestContext rc);
    
    
    /**
     * Get called just before closing the Hibernate request session and removing the requestContext
     * from the threadlocal
     * @param rc
     */
    public void end(RequestContext rc);
}

/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web;

public interface WebApplicationLifecycle {

    public void init();
    
    public void shutdown();
}

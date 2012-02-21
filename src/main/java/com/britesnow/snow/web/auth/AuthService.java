/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.auth;

import com.britesnow.snow.web.RequestContext;



/**
 * Interface that authenticate a request and return an authentication result object (usually a user object)
 * 
 * 
 * @author Jeremy Chone
 * 
 */
public interface AuthService<U> {

    
    /**
     * Responsible to get the Auth from the Request. Usually, will get/build the User from the session object,
     * and then create call the buildAuthFromUser.
     * @param rc
     * @return
     */
    public Auth<U> authRequest(RequestContext rc);
    

    

}

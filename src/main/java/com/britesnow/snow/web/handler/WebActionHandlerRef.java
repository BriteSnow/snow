/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.handler;


import java.lang.reflect.Method;

import com.britesnow.snow.web.handler.annotation.WebActionHandler;
import com.britesnow.snow.web.param.resolver.WebParamResolverRef;


public class WebActionHandlerRef extends WebHandlerRef{


    @SuppressWarnings("unused")
    private WebActionHandler    webAction;
    


    /*--------- Initialization ---------*/
    public WebActionHandlerRef(Object object, Method method,WebParamResolverRef[] webParamResolverRefs, 
                               WebActionHandler webAction) {
        super(object,method,webParamResolverRefs);
        this.webAction = webAction;
        
    }
    /*--------- /Initialization ---------*/
    
    

    
    


}

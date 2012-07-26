/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.handler;

import java.lang.reflect.Method;

import com.britesnow.snow.web.handler.annotation.FreemarkerMethodHandler;
import com.britesnow.snow.web.param.resolver.WebParamResolverRef;


public class FreemarkerMethodHandlerRef extends WebHandlerRef {

    FreemarkerMethodHandler freemarkerMethodHandler;
    
    public FreemarkerMethodHandlerRef(Object object, Method method,WebParamResolverRef[] webParamResolverRefs, FreemarkerMethodHandler freemarkerMethodHandler) {
        super(object, method, webParamResolverRefs);
        this.freemarkerMethodHandler = freemarkerMethodHandler;
    }    
    
}

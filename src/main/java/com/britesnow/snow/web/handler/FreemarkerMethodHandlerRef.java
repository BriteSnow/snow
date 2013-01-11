/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.handler;

import java.lang.reflect.Method;

import com.britesnow.snow.web.handler.annotation.FreemarkerMethodHandler;


public class FreemarkerMethodHandlerRef extends WebHandlerRef {

    FreemarkerMethodHandler freemarkerMethodHandler;
    
    public FreemarkerMethodHandlerRef(Class webClass, Method method,ParamDef[] paramDefs, FreemarkerMethodHandler freemarkerMethodHandler) {
        super(webClass, method, paramDefs);
        this.freemarkerMethodHandler = freemarkerMethodHandler;
    }    
    
}

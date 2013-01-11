/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.handler;

import java.lang.reflect.Method;

import com.britesnow.snow.web.handler.annotation.FreemarkerDirectiveHandler;


public class FreemakerDirectiveHandlerRef extends WebHandlerRef {

    FreemarkerDirectiveHandler webTemplateDirective;
    
    public FreemakerDirectiveHandlerRef(Class webClass, Method method,ParamDef[] paramDefs, FreemarkerDirectiveHandler webTemplateDirective) {
        super(webClass, method, paramDefs);
        this.webTemplateDirective = webTemplateDirective;
    }    
    
}

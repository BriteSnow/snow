/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.handler;

import java.lang.reflect.Method;

import com.britesnow.snow.web.handler.annotation.WebTemplateDirectiveHandler;
import com.britesnow.snow.web.param.resolver.WebParamResolverRef;


public class WebTemplateDirectiveHandlerRef extends WebHandlerRef {

    WebTemplateDirectiveHandler webTemplateDirective;
    
    public WebTemplateDirectiveHandlerRef(Object object, Method method,WebParamResolverRef[] webParamResolverRefs, WebTemplateDirectiveHandler webTemplateDirective) {
        super(object, method, webParamResolverRefs);
        this.webTemplateDirective = webTemplateDirective;
    }    
    
}

/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.handler;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.handler.annotation.WebActionHandler;
import com.britesnow.snow.web.param.WebParamRef;
import com.britesnow.snow.web.param.WebParameterParser;


public class WebActionHandlerRef extends BaseWebHandlerRef{


    @SuppressWarnings("unused")
    private WebActionHandler    webAction;
    


    /*--------- Initialization ---------*/
    public WebActionHandlerRef(Object object, Method method, Map<Class<? extends Annotation>,WebParameterParser> webParameterParserMap,
                               WebActionHandler webAction) {
        super(object,method,webParameterParserMap);

        this.webAction = webAction;
        
        initWebParamRefs();
    }
    /*--------- /Initialization ---------*/
    
    
    public Object invokeWebAction(RequestContext rc) throws Exception{
        Object[] paramValues = new Object[webArgRefs.size()];
        int i = 0;
        for (WebParamRef webParamRef : webArgRefs){
            paramValues[i++] = webParamRef.getValue(method, rc);
        }
        return method.invoke(webHandler, paramValues);
    }

    
    


}

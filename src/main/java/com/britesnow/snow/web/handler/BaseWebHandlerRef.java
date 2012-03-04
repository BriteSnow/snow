/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.britesnow.snow.web.param.OldWebParamRef;
import com.britesnow.snow.web.param.annotation.WebModel;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.britesnow.snow.web.param.annotation.WebPath;
import com.britesnow.snow.web.param.annotation.WebUser;


public class BaseWebHandlerRef {
    protected Object       webHandler;
    protected Method       method;
    protected List<OldWebParamRef> webArgRefs = new ArrayList<OldWebParamRef>();
    
    public BaseWebHandlerRef(Object webHandler, Method method) {
        this.webHandler = webHandler;
        this.method = method;
    }
    
    protected void initWebParamRefs(){
        if (method != null){
            Class[] paramClasses = method.getParameterTypes();
            Annotation[][] paramAnnotationsArray =  method.getParameterAnnotations();
            int i = 0;
            //for each method parameter class

            for (Class paramClass : paramClasses){
                Annotation webArgumentAnnotation = getWebArgumentAnnotationFromAnnotationArray(paramAnnotationsArray[i]);
                OldWebParamRef webParamRef;
                if (webArgumentAnnotation instanceof WebParam){
                    webParamRef = new OldWebParamRef((WebParam)webArgumentAnnotation,paramClass);
                }else if (webArgumentAnnotation instanceof WebUser){
                    webParamRef = new OldWebParamRef((WebUser)webArgumentAnnotation,paramClass);
                }else if (webArgumentAnnotation instanceof WebPath){
                    webParamRef = new OldWebParamRef((WebPath)webArgumentAnnotation,paramClass);
                }else if (webArgumentAnnotation instanceof WebModel){
                    webParamRef = new OldWebParamRef((WebModel)webArgumentAnnotation,paramClass);
                }
                else{
                    webParamRef = new OldWebParamRef(paramClass);
                }
                webArgRefs.add(webParamRef);
                
                i++;
            }
        }
    }
    
    private Annotation getWebArgumentAnnotationFromAnnotationArray(Annotation[] paramAnnotations){
        
        if (paramAnnotations != null){
            for (Annotation annotation : paramAnnotations){
                if (annotation instanceof WebParam){
                    return annotation;
                }
                if (annotation instanceof WebUser){
                    return annotation;
                }
                if (annotation instanceof WebPath){
                    return annotation;
                }
                if (annotation instanceof WebModel){
                    return annotation;
                }
            }
        }
        return null;
    }
    
    //for the WebHandlerMethodInterceptor
    public Method getMethod(){
    	return method;
    }
    
    /*--------- Invocation Methods ---------*/
    

    /*--------- /Invocation Methods ---------*/
}

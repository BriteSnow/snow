/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.britesnow.snow.web.param.WebParamRef;
import com.britesnow.snow.web.param.WebParameterParser;
import com.britesnow.snow.web.param.annotation.WebMap;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.britesnow.snow.web.param.annotation.WebPath;
import com.britesnow.snow.web.param.annotation.WebUser;


public class BaseWebHandlerRef {
    protected Object       webHandler;
    protected Method       method;
    protected List<WebParamRef> webArgRefs = new ArrayList<WebParamRef>();
    protected Map<Class<? extends Annotation>,WebParameterParser> webParameterParserMap;
    
    public BaseWebHandlerRef(Object webHandler, Method method, Map<Class<? extends Annotation>,WebParameterParser> webParameterParserMap) {
        this.webHandler = webHandler;
        this.method = method;
        this.webParameterParserMap = webParameterParserMap;
    }
    
    protected void initWebParamRefs(){
        if (method != null){
            Class[] paramClasses = method.getParameterTypes();
            Annotation[][] paramAnnotationsArray =  method.getParameterAnnotations();
            int i = 0;
            //for each method parameter class

            for (Class paramClass : paramClasses){
                Annotation webArgumentAnnotation = getWebArgumentAnnotationFromAnnotationArray(paramAnnotationsArray[i]);
                WebParamRef webParamRef;
                if (webArgumentAnnotation instanceof WebParam){
                    webParamRef = new WebParamRef((WebParam)webArgumentAnnotation,paramClass);
                }else if (webArgumentAnnotation instanceof WebUser){
                    webParamRef = new WebParamRef((WebUser)webArgumentAnnotation,paramClass);
                }else if (webArgumentAnnotation instanceof WebPath){
                    webParamRef = new WebParamRef((WebPath)webArgumentAnnotation,paramClass);
                }else if (webArgumentAnnotation instanceof WebMap){
                    webParamRef = new WebParamRef((WebMap)webArgumentAnnotation,paramClass);
                }else if (webArgumentAnnotation != null && webParameterParserMap.containsKey(webArgumentAnnotation.annotationType())) {
                    webParamRef = new WebParamRef(webParameterParserMap.get(webArgumentAnnotation.annotationType()), (Annotation) webArgumentAnnotation, paramClass);
                }
                else{
                    webParamRef = new WebParamRef(paramClass);
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
                if (annotation instanceof WebMap){
                    return annotation;
                }
                if (webParameterParserMap.containsKey(annotation.annotationType())) {
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

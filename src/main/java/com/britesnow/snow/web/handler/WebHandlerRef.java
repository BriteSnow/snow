package com.britesnow.snow.web.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


import com.britesnow.snow.util.AnnotationMap;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.param.resolver.WebParamResolverRef;
import com.google.common.base.Throwables;

public class WebHandlerRef {
    
    protected Object webHandler;
    protected Method method;
    private WebParamResolverRef[] paramResolverRefs;
    
    WebHandlerRef(Object webHandlerObject, Method method, WebParamResolverRef[] paramResolverRefs) {
        this.webHandler = webHandlerObject;
        this.method = method;
        this.paramResolverRefs = paramResolverRefs;
    }
    
    
    public Object getHandlerObject(){
        return webHandler;
    }
    
    public Method getHandlerMethod(){
        return method;
    }    
    
    public Object invoke(RequestContext rc){
        Object result = null;
        
        // resolve the argument values
        Class[] paramTypes = method.getParameterTypes();
        Annotation[][] paramAnnotationsArray = method.getParameterAnnotations();
        Object[] values = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length ; i++){
            Class paramType = paramTypes[i];
            AnnotationMap annotationMap = new AnnotationMap(paramAnnotationsArray[i]);
            WebParamResolverRef paramResolverRef = paramResolverRefs[i];
            
            Object value = paramResolverRef.invoke(annotationMap, paramType, rc);
            
            values[i] = value;
        }
        
        // invoke the WebHandler method
        try {
            result = method.invoke(webHandler, values);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        
        return result;
        
    }

}

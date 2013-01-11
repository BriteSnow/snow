package com.britesnow.snow.web.handler;

import java.lang.reflect.Method;

public class WebHandlerRef {
    
    protected Class webClass;
    protected Method method;
    private ParamDef[] paramDefs;
    
    WebHandlerRef(Class webClass, Method method, ParamDef[] paramDefs) {
        this.webClass = webClass;
        this.method = method;
        this.paramDefs = paramDefs;
    }
    
    
    public Method getHandlerMethod(){
        return method;
    }    
    
    public ParamDef[] getParamDefs(){
        return paramDefs;
    }
    
    public Class getWebClass(){
        return webClass;
    }
    
    /*
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
    */

}

package com.britesnow.snow.web.param.resolver;

import java.lang.reflect.Method;

import com.britesnow.snow.util.AnnotationMap;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.param.resolver.annotation.WebParamResolver;
import com.google.common.base.Throwables;

public class WebParamResolverRef {
    
    // set a constructor
    private final WebParamResolver webParamResolverAnnotation;
    private final Object resolverObject; 

    private final Method resolverMethod;
    
    // 
    private final Class returnType;
    
    
    public WebParamResolverRef(WebParamResolver webParamResolverAnnotation,Object resolverObject, Method resolverMethod){
        this.webParamResolverAnnotation = webParamResolverAnnotation;
        this.resolverObject = resolverObject;
        this.resolverMethod = resolverMethod;
        
        returnType = resolverMethod.getReturnType();
    }
    
    
    // --------- Public --------- //
    public Class getReturnType(){
        return returnType;
    }
    
    public Class[] getAnnotatedWith(){
        return webParamResolverAnnotation.annotatedWith();
    }
    
    public Object invoke(AnnotationMap annotationMap, Class paramType, RequestContext rc){
        Object[] args = new Object[]{annotationMap, paramType,rc};
        try {
            return resolverMethod.invoke(resolverObject, args);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        
    }
    // --------- /Public --------- //    
    
    

}

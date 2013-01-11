package com.britesnow.snow.web.param.resolver;

import java.lang.reflect.Method;

import com.britesnow.snow.web.param.resolver.annotation.WebParamResolver;

public class WebParamResolverRef {
    
    // set a constructor
    private final WebParamResolver webParamResolverAnnotation;
    private final Class resolverClass; 

    private final Method resolverMethod;
    
    // 
    private final Class returnType;
    
    
    public WebParamResolverRef(WebParamResolver webParamResolverAnnotation,Class resolverClass, Method resolverMethod){
        this.webParamResolverAnnotation = webParamResolverAnnotation;
        this.resolverClass = resolverClass;
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
    
    
    public Class getWebClass(){
        return resolverClass;
    }
    
    public Method getWebMethod(){
        return resolverMethod;
    }
    // --------- /Public --------- //    
    
    

}

package com.britesnow.snow.web.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.inject.Inject;

import com.britesnow.snow.web.param.resolver.WebParamResolverRef;
import com.britesnow.snow.web.param.resolver.WebParamResolverRegistry;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;

@Singleton
public class ParamDefBuilder {
    
    @Inject
    private Injector injector; 
    
    @Inject
    private WebParamResolverRegistry paramResolverRegistry; 
    
    
    public ParamDef[] buildParamDefs(Method m){
        int paramCount = m.getParameterTypes().length;
        ParamDef[] paramDefs = new ParamDef[paramCount];
        for (int i = 0; i < paramCount; i++){
            ParamDef paramDef = buildParamDef(m, i);
            paramDefs[i] = paramDef;
        }
        return paramDefs;
    }
    
    private ParamDef buildParamDef(Method m,int idx){
        
        Class paramType = m.getParameterTypes()[idx];
        Annotation[] paramAnnotations = m.getParameterAnnotations()[idx];
        
        Key key = null;
        
        WebParamResolverRef paramResolverRef = paramResolverRegistry.getWebParamResolverRef(m, idx);
        
        // if it is a Google Guice, check if there are provider for the annotations
        System.out.println("buildParamDef: " + paramType + " " + paramResolverRef);
        if (paramResolverRef == null){
            if (paramAnnotations.length > 0){
                for (Annotation an : paramAnnotations){
                    key = Key.get(paramType,an);
                    System.out.println("binding: " + paramType + " " + key);
                    //Binding b = injector.getBinding(key);
                    
                }
            }
        }
        
        ParamDef paramDef = new ParamDef(paramType,paramAnnotations,paramResolverRef);
        
        return paramDef; 
        
    }

}

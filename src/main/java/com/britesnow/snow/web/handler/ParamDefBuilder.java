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
    
    
    public ParamDef[] buildParamDefs(Method m, boolean withParamResolver){
        int paramCount = m.getParameterTypes().length;
        ParamDef[] paramDefs = new ParamDef[paramCount];
        for (int i = 0; i < paramCount; i++){
            ParamDef paramDef = buildParamDef(m, i,withParamResolver);
            paramDefs[i] = paramDef;
        }
        return paramDefs;
    }
    
    private ParamDef buildParamDef(Method m,int idx,boolean withParamResolver){
        
        Class paramType = m.getParameterTypes()[idx];
        Annotation[] paramAnnotations = m.getParameterAnnotations()[idx];
        
        Key key = null;
        
        WebParamResolverRef paramResolverRef = null;
        
        if (withParamResolver){
            paramResolverRef = paramResolverRegistry.getWebParamResolverRef(m, idx);
        }
        
        // if it is a Google Guice, check if there are provider for the annotations
        if (paramResolverRef == null){
            if (paramAnnotations.length > 0){
                for (Annotation an : paramAnnotations){
                    key = Key.get(paramType,an);
                    break; // for now, just take the first.
                    // TODO: Has to check if the key is bound, if not, go to the next annotation
                    //Binding b = injector.getBinding(key);
                    //Provider b = injector.getProvider(key);
                    //System.out.println("binding: " + paramType + " " + key + " " + b);
                    
                }
            }
        }
        
        ParamDef paramDef = new ParamDef(paramType,paramAnnotations,paramResolverRef,key);
        
        return paramDef; 
        
    }

}

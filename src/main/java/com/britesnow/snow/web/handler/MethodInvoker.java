package com.britesnow.snow.web.handler;

import java.lang.reflect.Method;

import javax.inject.Inject;

import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.hook.HookRef;
import com.britesnow.snow.web.param.resolver.WebParamResolverRef;
import com.google.common.base.Throwables;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;


@Singleton
public class MethodInvoker {

    @Inject
    private Injector injector;
    
    public Object invokeWebHandler(WebHandlerRef webHandlerRef,RequestContext rc){
        return invokeMethod(webHandlerRef.getWebClass(),webHandlerRef.getHandlerMethod(),webHandlerRef.getParamDefs(),rc);
    }
    
    public Object invokeHook(HookRef hookRef, RequestContext rc){
        return invokeMethod(hookRef.getCls(), hookRef.getMethod(), hookRef.getParamDefs(),rc);
    }
    
    
    private Object invokeMethod(Class c, Method m, ParamDef[] paramDefs, RequestContext rc){
        Object result = null;
        
        Object o = injector.getInstance(c);
        
        Object[] values = new Object[paramDefs.length];
        for (int i = 0; i < paramDefs.length; i++){
            if (rc != null){
                values[i] = resolveParamDef(paramDefs[i],rc);
            }else{
                values[i] = resolveParamDef(paramDefs[i]);
            }
        }
        
        try {
            result = m.invoke(o, values);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        
        return result;
    }
    
    // will try the WebParamResolver first, and then, fall back on the guice one.
    private Object resolveParamDef(ParamDef paramDef, RequestContext rc){
        
        Object result = null;
        
        WebParamResolverRef webParamResolverRef = paramDef.getWebParamResolverRef();
        if (webParamResolverRef != null){
            result = webParamResolverRef.invoke(paramDef.getAnnotationMap(), paramDef.getParamType(), rc);
        }else{
            result = resolveParamDef(paramDef);
        }
        
        return result;
    }
    
    private Object resolveParamDef(ParamDef paramDef){
        Key key = paramDef.getKey();
        if (key != null){
            return injector.getInstance(key);
        }else{
            return injector.getInstance(paramDef.getParamType());
        }
    }
}

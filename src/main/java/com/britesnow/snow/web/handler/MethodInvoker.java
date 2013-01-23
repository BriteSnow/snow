package com.britesnow.snow.web.handler;

import java.lang.reflect.Method;

import javax.inject.Inject;

import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.exception.WebExceptionCatcherRef;
import com.britesnow.snow.web.exception.WebExceptionContext;
import com.britesnow.snow.web.hook.HookRef;
import com.britesnow.snow.web.param.resolver.WebParamResolverRef;
import com.britesnow.snow.web.rest.WebRestRef;
import com.britesnow.snow.web.rest.WebSerializerRef;
import com.google.common.base.Throwables;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;


@Singleton
public class MethodInvoker {

    @Inject
    private Injector injector;
    
    
    public Object invokeWebRest(WebRestRef webRestRef, RequestContext rc){
        return invokeMethod(webRestRef.getWebClass(),webRestRef.getHandlerMethod(),webRestRef.getParamDefs(),rc);
    }
    
    public Object invokeWebHandler(WebHandlerRef webHandlerRef,RequestContext rc){
        return invokeMethod(webHandlerRef.getWebClass(),webHandlerRef.getHandlerMethod(),webHandlerRef.getParamDefs(),rc);
    }
    
    public Object invokeHook(HookRef hookRef, RequestContext rc){
        return invokeMethod(hookRef.getCls(), hookRef.getMethod(), hookRef.getParamDefs(),rc);
    }
    
    public void invokeWebException(WebExceptionCatcherRef ecRef, Throwable ecT,  WebExceptionContext webExceptionContext,RequestContext rc){
        Object[] values = new Object[] { ecT, webExceptionContext, rc };
        try {
            Object webObject = injector.getInstance(ecRef.getWebClass());
            ecRef.getWebMethod().invoke(webObject, values);
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }        
    }
    
    public void invokeWebSerializer(WebSerializerRef ref, RequestContext rc){
        Object result = rc.getResult();
        Object[] values = new Object[]{result,rc};
        try {
            Object webObject = injector.getInstance(ref.getWebClass());
            ref.getHandlerMethod().invoke(webObject, values);
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }           
    }
    
    
    private Object invokeMethod(Class c, Method m, ParamDef[] paramDefs, RequestContext rc){
        Object result = null;
        
        Object o = injector.getInstance(c);
        
        Object[] values = new Object[paramDefs.length];
        for (int i = 0; i < paramDefs.length; i++){
            ParamDef paramDef = paramDefs[i];
            if (rc != null && paramDef.hasWebParamResolver()){
                values[i] = webResolveParamDef(paramDef,rc);
            }else{
                values[i] = guiceResolveParamDef(paramDef);
            }
        }
        
        try {
            result = m.invoke(o, values);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        
        return result;
    }
    
    private Object webResolveParamDef(ParamDef paramDef, RequestContext rc){
        Object[] values = new Object[]{paramDef.getAnnotationMap(), paramDef.getParamType(),rc};
        Object result = null;
        WebParamResolverRef ref = paramDef.getWebParamResolverRef();
        
        Object o = injector.getInstance(ref.getWebClass());
        try {
            result = ref.getWebMethod().invoke(o, values);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }        
        
        return result;
    }
    

    private Object guiceResolveParamDef(ParamDef paramDef){
        Key key = paramDef.getKey();
        if (key != null){
            return injector.getInstance(key);
        }else{
            return injector.getInstance(paramDef.getParamType());
        }
    }
}

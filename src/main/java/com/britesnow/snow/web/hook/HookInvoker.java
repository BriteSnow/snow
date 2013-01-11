package com.britesnow.snow.web.hook;

import java.util.List;

import javax.inject.Inject;

import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.handler.MethodInvoker;
import com.google.inject.Singleton;

@Singleton
public class HookInvoker {

    @Inject
    private HookRegistry hookRegistry;
    
    @Inject
    private MethodInvoker methodInvoker;
    
    
    public void invokeAppHooks(AppStep step){
        List<AppHookRef> appHookRefs = hookRegistry.getAppHooks(step);
        
        if (appHookRefs != null){
            for (AppHookRef appHookRef : appHookRefs){
                methodInvoker.invokeHook(appHookRef,null);
            }
        }
    }
    
    public void invokeReqHooks(ReqStep step, RequestContext rc){
        List<ReqHookRef> reqHookRefs = hookRegistry.getRequestHooks(step);
        if (reqHookRefs != null){
            for (ReqHookRef reqHookRef : reqHookRefs){
                methodInvoker.invokeHook(reqHookRef, rc);
            }
        }
    }
    
    
    
}

package com.britesnow.snow.web.hook;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.britesnow.snow.web.handler.ParamDefBuilder;
import com.britesnow.snow.web.hook.annotation.WebApplicationHook;
import com.britesnow.snow.web.hook.annotation.WebRequestHook;

/**
 * Maintains the App and Request hooks, organized by Step.
 * 
 * Note that the add*** methods are not designed to be multi thread safe as they must be called only from the
 * initialization phase (by Snow internal).
 * 
 */
@Singleton
public class HookRegistry {
    
    @Inject
    private ParamDefBuilder paramDefBuilder;

    private Map<AppPhase, List<AppHookRef>> appHooksDic = new HashMap<AppPhase, List<AppHookRef>>();
    private Map<ReqPhase, List<ReqHookRef>> reqHooksDic = new HashMap<ReqPhase, List<ReqHookRef>>();

    public List<AppHookRef> getAppHooks(AppPhase step){
        return appHooksDic.get(step);
    }
    
    public List<ReqHookRef> getRequestHooks(ReqPhase step){
        return reqHooksDic.get(step);
    }
    
    
    
    
    // --------- Initialization Methods --------- //
    public void addAppHook(Class cls,Method m,WebApplicationHook webApplicationHook){
        AppHookRef appHookRef = new AppHookRef(cls, m, paramDefBuilder.buildParamDefs(m,false), webApplicationHook.phase(),webApplicationHook.on());
        addAppHookRef(appHookRef);
    }  
    
    public void addAppHookRef(AppHookRef appHookRef) {
        List<AppHookRef> hooks = appHooksDic.get(appHookRef.getStep());
        if (hooks == null) {
            hooks = new ArrayList<AppHookRef>();
            appHooksDic.put(appHookRef.getStep(), hooks);
        }
        hooks.add(appHookRef);
    }

    
    public void addReqHook(Class cls,Method m,WebRequestHook webRequestHook){
        ReqHookRef reqHookRef = new ReqHookRef(cls, m, paramDefBuilder.buildParamDefs(m,true), webRequestHook.phase(),webRequestHook.on());
        addReqHookRef(reqHookRef);
    }
    
    private void addReqHookRef(ReqHookRef reqHookRef) {
        List<ReqHookRef> hooks = reqHooksDic.get(reqHookRef.getStep());
        if (hooks == null) {
            hooks = new ArrayList<ReqHookRef>();
            reqHooksDic.put(reqHookRef.getStep(), hooks);
        }
        hooks.add(reqHookRef);
    }
    // --------- /Initialization Methods --------- //

}

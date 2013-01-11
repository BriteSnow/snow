package com.britesnow.snow.test.singletest.classes;

import java.util.Map;

import javax.inject.Singleton;

import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.hook.ReqStep;
import com.britesnow.snow.web.hook.annotation.WebRequestHook;
import com.britesnow.snow.web.param.annotation.WebModel;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.google.inject.name.Named;

@Singleton
public class SimpleWebRequestHooks{
    
    
    @WebModelHandler(startsWith="/info")
    public void info(@WebModel Map m,@WebParam("id")Long id,@Named("testVal")String testVal){
        System.out.println("testVal: -" + testVal + "-");
        System.out.println("id: -" + id + "-");
        m.put("webModelHandler-info", true);
    }
    
    @WebRequestHook(step=ReqStep.START)
    public void reqHookStart(@WebModel Map m){
        m.put("webRequestHook-start", true);
    }
}

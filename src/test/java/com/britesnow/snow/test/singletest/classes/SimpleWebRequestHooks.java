package com.britesnow.snow.test.singletest.classes;

import java.util.Map;

import javax.inject.Singleton;

import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.hook.On;
import com.britesnow.snow.web.hook.ReqPhase;
import com.britesnow.snow.web.hook.annotation.WebRequestHook;
import com.britesnow.snow.web.param.annotation.WebModel;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.google.inject.name.Named;

@Singleton
public class SimpleWebRequestHooks{
    
    
    @WebModelHandler(startsWith="/info")
    public void info(@WebModel Map m,@WebParam("id")Long id,@SampleStringValue String sampleVal, @Named("testVal")String testVal){
        m.put("id", id);
        m.put("sampleStringValue", sampleVal);
        m.put("testVal", testVal);
        m.put("webModelHandler-info", true);
    }
    
    @WebRequestHook(phase=ReqPhase.START,on=On.BEFORE)
    public void reqHookStart(@WebModel Map m){
        m.put("webRequestHook-start", "aaa");
    }

    @WebRequestHook(phase=ReqPhase.START)
    public void reqHookStartAfter(@WebModel Map m){
        String v = m.get("webRequestHook-start") + "bbb";
        m.put("webRequestHook-start", v);
    }
    
}

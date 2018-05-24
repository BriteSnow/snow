package com.britesnow.snow.test.app.simpleapp.web;

import java.util.Map;

import javax.inject.Singleton;

import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.web.HttpMethod;
import com.britesnow.snow.web.param.annotation.PathVar;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.britesnow.snow.web.rest.annotation.*;

@Singleton
public class WebRestHandlers {


    @WebOptions("/api/echoParam1")
    public Map optionsEchoParam1(@WebParam("param1") String param1){
        Map result = MapUtil.mapIt("method","optionsEchoParam1");
        return result;
    }

    @WebPatch("/api/echoParam1")
    public Map patchEchoParam1(@WebParam("param1") String param1){
        Map result = MapUtil.mapIt("method","patchEchoParam1");
        return result;
    }

    @WebGet("/api/echoParam1")
    public Map simpleEchoParam1(@WebParam("param1")String param1){
        Map result = MapUtil.mapIt("param1",param1,"simpleWebGet",true);
        return result;
    }
    
    @WebPost("/api/set-val1")
    public Map simpleSetVal1(@WebParam("val1")String val1){
        Map result = MapUtil.mapIt("val1",val1,"simpleWebPost",true);
        return result;
    }
    
    @WebPut("/api/put-key-value")
    public Map putKeyValue(@WebParam("key")String key, @WebParam("value")String value){
        Map result = MapUtil.mapIt(key,value,"simpleWebPut",true);
        return result;
    }     

    
    @WebDelete("/api/delete-entity")
    public Map deleteEntity(@WebParam("entity_id")Long entityId){
        Map result = MapUtil.mapIt("deleted_entity_id",entityId);
        return result;
    }  
    
    @WebGet()
    public Map fromMethodName(@WebParam("info")String info){
        return MapUtil.mapIt("info-echo",info);
    }
    
    @WebGet("/post-and-get")
    @WebPost("/post-and-get")
    public Map postAndGet(HttpMethod method, @WebParam("info")String info ){
        return MapUtil.mapIt("method-echo",method,"info-echo",info);
    }
    
    @WebDelete("/delete-item-{itemId}-")
    public String deleteItem(@PathVar("itemId") Long itemId){
        return "{\"deletedItemId\":"  + itemId + "}";
    }
    
}

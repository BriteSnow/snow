package com.britesnow.snow.test.singletest.classes;

import java.util.Map;

import javax.inject.Singleton;

import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.britesnow.snow.web.rest.annotation.WebGet;

@Singleton
public class SimpleWebRest {

    @WebGet("/api/echoParam1")
    public Map simpleEchoParam1(@WebParam("param1")String param1){
        Map result = MapUtil.mapIt("param1",param1,"simpleWebGet",true);
        return result;
    }
    
}

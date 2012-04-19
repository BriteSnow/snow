package com.britesnow.snow.test.app.simpleapp.web;

import java.util.Map;

import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.param.annotation.WebModel;
import com.britesnow.snow.web.param.annotation.WebParam;

public class WebParamResolverTestWebHandlers {

    public enum TestEnum{
        foo,bar;
    }
    
    @WebModelHandler(startsWith="/testEnumWebParam")
    public void testEnumWebParam(@WebModel Map m, @WebParam("testenum") TestEnum testenum){
        m.put("result", testenum.name());
    }
    
}

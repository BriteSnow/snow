package com.britesnow.snow.test.singletest.classes;

import java.util.Map;

import javax.inject.Singleton;

import net.sf.json.JSONObject;

import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.param.annotation.WebModel;
import com.britesnow.snow.web.param.annotation.WebParam;


@Singleton
public class JSONObjectWebHandlers {

    @WebModelHandler(startsWith="/test/jsonobject")
    public void jsonObject(@WebModel Map m, @WebParam("jsonstr") JSONObject jsonObj){
        m.put("success", true);
    }
}

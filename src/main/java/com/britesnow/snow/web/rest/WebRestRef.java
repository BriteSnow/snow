package com.britesnow.snow.web.rest;

import java.lang.reflect.Method;

import com.britesnow.snow.web.handler.ParamDef;
import com.britesnow.snow.web.handler.WebHandlerRef;
import com.britesnow.snow.web.rest.annotation.WebGet;
import com.britesnow.snow.web.rest.annotation.WebPost;

public class WebRestRef extends WebHandlerRef {

    private WebGet webGet;
    private WebPost webPost;

    
    WebRestRef(Class webClass, Method method, ParamDef[] paramDefs, WebPost webPost) {
        super(webClass, method, paramDefs);
        this.webPost = webPost;
    }
    
    WebRestRef(Class webClass, Method method, ParamDef[] paramDefs, WebGet webGet) {
        super(webClass, method, paramDefs);
        this.webGet = webGet;
    }

    public WebGet getWebGet() {
        return webGet;
    }

    public WebPost getWebPost() {
        return webPost;
    }
    
    
}

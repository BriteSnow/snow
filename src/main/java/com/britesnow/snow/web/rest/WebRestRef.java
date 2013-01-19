package com.britesnow.snow.web.rest;

import java.lang.reflect.Method;

import com.britesnow.snow.web.handler.ParamDef;
import com.britesnow.snow.web.handler.WebHandlerRef;
import com.britesnow.snow.web.rest.annotation.WebDelete;
import com.britesnow.snow.web.rest.annotation.WebGet;
import com.britesnow.snow.web.rest.annotation.WebPost;
import com.britesnow.snow.web.rest.annotation.WebPut;

@SuppressWarnings("unused")
public class WebRestRef extends WebHandlerRef {

    // for now, we do not uses this properties
    private WebGet webGet;
    private WebPost webPost;
    private WebDelete webDelete;
    private WebPut webPut;

    
    WebRestRef(Class webClass, Method method, ParamDef[] paramDefs, WebPost webPost) {
        super(webClass, method, paramDefs);
        this.webPost = webPost;
    }
    
    WebRestRef(Class webClass, Method method, ParamDef[] paramDefs, WebGet webGet) {
        super(webClass, method, paramDefs);
        this.webGet = webGet;
    }
    
    WebRestRef(Class webClass, Method method, ParamDef[] paramDefs, WebDelete webDelete) {
        super(webClass, method, paramDefs);
        this.webDelete = webDelete;
    }

    WebRestRef(Class webClass, Method method, ParamDef[] paramDefs, WebPut webPut) {
        super(webClass, method, paramDefs);
        this.webPut = webPut;
    }
    
    
}

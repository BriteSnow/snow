package com.britesnow.snow.web.handler;

import java.lang.reflect.Method;

public class WebHandlerContext {

    private Object handlerObject;
    private Method handlerMethod;
    private WebHandlerType handlerType;
    
    public WebHandlerContext(WebHandlerType handlerType, Object handlerObject, Method handlerMethod){
        this.handlerType = handlerType;
        this.handlerObject = handlerObject;
        this.handlerMethod = handlerMethod;
    }
    
    public Object getHandlerObject(){
        return handlerObject;
    }
    
    public Method getHandlerMethod(){
        return handlerMethod;
    }
    
    public WebHandlerType getHandlerType(){
        return handlerType;
    }
}

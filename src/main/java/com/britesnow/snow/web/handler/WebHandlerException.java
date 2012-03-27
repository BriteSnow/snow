package com.britesnow.snow.web.handler;

public class WebHandlerException extends RuntimeException {

    private WebHandlerContext webHandlerContext;
    private Throwable cause;
    
    public WebHandlerException(WebHandlerContext webHandlerContext, Throwable cause){
        this.webHandlerContext = webHandlerContext;
        this.cause = cause;
    }
    
    public WebHandlerContext getWebHandlerContext(){
        return webHandlerContext;
    }
    
    public Throwable getCause(){
        return cause;
    }
}

package com.britesnow.snow.web.handler;

public class WebHandlerException extends RuntimeException {

    private static final long serialVersionUID = 8073725890556879904L;
    
    
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

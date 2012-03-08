package com.britesnow.snow.web.exception;

import com.britesnow.snow.web.handler.WebHandlerContext;

/**
 * For now empty. 
 * 
 * @author jeremychone
 *
 */
public class WebExceptionContext {

    private WebHandlerContext webHandlerContext;
    
    public WebExceptionContext(WebHandlerContext webHandlerContext){
        this.webHandlerContext = webHandlerContext;
    }
    
    
    public WebHandlerContext getWebHandlerContext(){
        return webHandlerContext;
    }
}

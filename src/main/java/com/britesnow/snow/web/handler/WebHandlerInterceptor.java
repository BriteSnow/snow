package com.britesnow.snow.web.handler;

import com.britesnow.snow.web.RequestContext;

public interface WebHandlerInterceptor {

    
    public boolean beforeWebHandler(WebHandlerContext context, RequestContext rc);
    
    public void afterWebHandler(WebHandlerContext context, RequestContext rc);
    
    
}

package com.britesnow.snow.web;

import java.lang.reflect.Method;

import javax.inject.Singleton;

import com.britesnow.snow.web.param.resolver.WebParamResolverRegistry;
import com.google.inject.Inject;


@Singleton
public class WebHandlerInvoker {

    @Inject
    WebParamResolverRegistry webParamResolverManager;
    
    public Object invokeWebHandler(Object webHandlerObject,Method method,RequestContext rc){
      Object result = null;
      
      return result;
    }
}

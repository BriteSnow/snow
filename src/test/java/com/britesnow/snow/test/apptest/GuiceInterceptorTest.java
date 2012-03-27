package com.britesnow.snow.test.apptest;

import static org.junit.Assert.*;

import java.util.Map;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.web.CurrentRequestContextHolder;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.param.resolver.annotation.WebParamResolver;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class GuiceInterceptorTest extends SnowTestSupport{

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp",new GuiceInceptorModule());
    }
    
    @Test
    public void testInterceptor(){
        Map result;
        RequestContextMock rc;
        
        // test getting contact id = 1 (Mike)
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/contact.json");
        rc.setParamMap(MapUtil.mapIt("id", "1"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        
        System.out.println(result);
        // assert that it worked normally
        assertEquals("Mike", MapUtil.getDeepValue(result, "contact.name"));
        // assert we got the WebModelHandler Interceptor working
        assertEquals("true", MapUtil.getDeepValue(result, "WebModelHandlerInterceptor"));
        assertEquals("true", MapUtil.getDeepValue(result, "WebParamResolverInterceptor"));
        
    }    
    
}


class GuiceInceptorModule extends AbstractModule{

    @Override
    protected void configure() {
        MethodInterceptor webModelHandlerInterceptor =new WebModelHandlerInterceptor();
        requestInjection(webModelHandlerInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(WebModelHandler.class), webModelHandlerInterceptor);
        
        MethodInterceptor webParamInterceptor = new WebParamInterceptor();
        requestInjection(webParamInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(WebParamResolver.class), webParamInterceptor);
    }
    
}


class WebParamInterceptor implements MethodInterceptor{
    @Inject
    private CurrentRequestContextHolder rcHolder;
    
    @Override
    public Object invoke(MethodInvocation invoc) throws Throwable {
        RequestContext rc = rcHolder.getCurrentRequestContext();
        Map webModel = rc.getWebModel();
        
        webModel.put("WebParamResolverInterceptor",true);
        
        Object result =  invoc.proceed();
        
        return result;
    }
    
}

class WebModelHandlerInterceptor  implements MethodInterceptor{
    @Inject
    private CurrentRequestContextHolder rcHolder;
    
    @Override
    public Object invoke(MethodInvocation invoc) throws Throwable {
        RequestContext rc = rcHolder.getCurrentRequestContext();
        Map webModel = rc.getWebModel();
        
        webModel.put("WebModelHandlerInterceptor",true);
        
        Object result =  invoc.proceed();
        
        return result;
    }
    
}



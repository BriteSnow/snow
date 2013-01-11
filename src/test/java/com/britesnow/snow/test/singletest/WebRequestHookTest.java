package com.britesnow.snow.test.singletest;

import java.util.Map;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.test.singletest.classes.SimpleWebRequestHooks;
import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.web.binding.WebClasses;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class WebRequestHookTest extends SnowTestSupport {

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp",new CustomModule());
    }
    
    @Test
    public void testWebModelInfoJson(){
        RequestContextMock rc;
        Map result;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/info.json");
        rc.setParamMap(MapUtil.mapIt("id", "1"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        
        Assert.assertTrue((Boolean) result.get("webModelHandler-info"));
        Assert.assertTrue((Boolean) result.get("webRequestHook-start"));
    }
    
    
}


class CustomModule extends AbstractModule{

    @Override
    protected void configure() {
    }
    
    @Provides
    @WebClasses
    public Class[] provideWebClasses(){
        return new Class[] {SimpleWebRequestHooks.class};
    }
    
}


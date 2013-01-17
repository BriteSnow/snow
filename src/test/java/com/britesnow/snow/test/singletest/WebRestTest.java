package com.britesnow.snow.test.singletest;

import java.util.Map;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.test.singletest.classes.SimpleWebRest;
import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.web.binding.WebClasses;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class WebRestTest extends SnowTestSupport {

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp", new WebRestTestModule());
    }
    
    @Test
    public void testWebGet(){
        RequestContextMock rc;
        Map result;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/api/echoParam1");
        rc.setParamMap(MapUtil.mapIt("param1", "123Hello"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        
        Assert.assertEquals("123Hello",result.get("param1"));
    }
    
    
    @Test
    public void testWebPost(){
        RequestContextMock rc;
        Map result;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/api/echoParam1");
        rc.setParamMap(MapUtil.mapIt("param1", "123Hello"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        
        
        Assert.assertEquals("123Hello",result.get("param1"));        
    }
    
    
}


class WebRestTestModule extends AbstractModule{

    @Override
    protected void configure() {
    }
    
    @Provides
    @WebClasses
    public Class[] provideWebClasses(){
        return new Class[] {SimpleWebRest.class};
    }
    

}


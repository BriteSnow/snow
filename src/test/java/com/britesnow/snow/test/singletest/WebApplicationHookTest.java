package com.britesnow.snow.test.singletest;


import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.test.singletest.classes.SimpleWebAppHooks;
import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.web.binding.WebClasses;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class WebApplicationHookTest extends SnowTestSupport {

    @BeforeClass
    public static void initTestClass() throws Exception {
        Map map = MapUtil.mapIt("testVal","testValue1");
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp",map,new CustomAppModule());
    }
    
    @Test
    public void testWebModelInfoJson(){
        RequestContextMock rc;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/info.json");
        rc.setParamMap(MapUtil.mapIt("id", "1"));
        webController.service(rc);
        Assert.assertTrue(SimpleWebAppHooks.appStates[0].init);
    }
    
    @AfterClass
    public static void shutDownTestClass(){
        try {
            SnowTestSupport.shutdownWebApplicaton();
            Assert.assertNotNull(SimpleWebAppHooks.appStates[1]);
            Assert.assertFalse(SimpleWebAppHooks.appStates[1].init);
            Assert.assertTrue(SimpleWebAppHooks.appStates[1].shutdown);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }
    
}


class CustomAppModule extends AbstractModule{

    @Override
    protected void configure() {
        
    }
    
    @Provides
    @WebClasses
    public Class[] provideWebClasses(){
        return new Class[] {SimpleWebAppHooks.class};
    }
    
}




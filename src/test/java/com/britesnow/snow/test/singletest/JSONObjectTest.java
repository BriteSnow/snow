package com.britesnow.snow.test.singletest;

import java.util.Map;

import org.junit.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.test.singletest.classes.JSONObjectParamResolvers;
import com.britesnow.snow.test.singletest.classes.JSONObjectWebHandlers;
import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.web.binding.WebClasses;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class JSONObjectTest extends SnowTestSupport {

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp", new JSONObjectTestModule());
    }
    
    @Test
    public void testJSONObject(){
        RequestContextMock rc;
        Map result;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/test/jsonobject.json");
        rc.setParamMap(MapUtil.mapIt("jsonstr", "{'name':'name1'}"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        
        Assert.assertTrue((Boolean) result.get("success"));
    }
    
}


class JSONObjectTestModule extends AbstractModule{

    @Override
    protected void configure() {
    }
    
    @Provides
    @WebClasses
    public Class[] provideWebClasses(){
        return new Class[] {JSONObjectWebHandlers.class, JSONObjectParamResolvers.class};
    }
    

}


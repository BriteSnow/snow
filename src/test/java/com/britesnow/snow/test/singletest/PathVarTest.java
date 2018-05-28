package com.britesnow.snow.test.singletest;

import java.util.Map;

import org.junit.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.test.singletest.classes.PathVarWebRests;
import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.web.binding.WebClasses;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class PathVarTest extends SnowTestSupport {

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp", new PathVarTestModule());
    }
    
    @Test
    public void testGetItem(){
        RequestContextMock rc;
        Map result;

        // @WegGet("/rest/{entity}");
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/rest/Task");
        webController.service(rc);
        result = rc.getResponseAsJson();
        Assert.assertEquals("restListEntities", result.get("method"));

        // @WegGet("/rest/{entity}/{id}");
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/rest/Task/123");
        webController.service(rc);
        result = rc.getResponseAsJson();
        Assert.assertEquals("restGetEntity", result.get("method"));


        // @WebGet("/get-{entity}-{id}")
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/get-item-222");
        webController.service(rc);
        result = rc.getResponseAsJson();
        Assert.assertEquals("/get-{entity}-{id}", result.get("path"));
        Assert.assertEquals("item", result.get("entity"));
        Assert.assertEquals("222", result.get("id"));


        // @WebGet("/get-{entity}-{id}--")
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/get-item-222--");
        webController.service(rc);
        result = rc.getResponseAsJson();
        Assert.assertEquals("/get-{entity}-{id}--", result.get("path"));
        Assert.assertEquals("item", result.get("entity"));
        Assert.assertEquals("222", result.get("id"));


        // @WebGet("/get-task-{id}")
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/get-task-111");
        webController.service(rc);
        result = rc.getResponseAsJson();
        Assert.assertEquals("/get-task-{id}", result.get("path"));
        Assert.assertEquals(111, result.get("id"));

        // @WebGet("/get-task-{id}--")
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/get-task-111--");
        webController.service(rc);
        result = rc.getResponseAsJson();
        Assert.assertEquals("/get-task-{id}--", result.get("path"));
        Assert.assertEquals(111, result.get("id"));

        // @WebGet("/get-task-123123")
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/get-task-123123");
        webController.service(rc);
        result = rc.getResponseAsJson();
        Assert.assertEquals("/get-task-123123", result.get("path"));


    }
    
}


class PathVarTestModule extends AbstractModule{

    @Override
    protected void configure() {
    }
    
    @Provides
    @WebClasses
    public Class[] provideWebClasses(){
        return new Class[] {PathVarWebRests.class};
    }
    

}


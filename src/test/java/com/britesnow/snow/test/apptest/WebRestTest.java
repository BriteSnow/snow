package com.britesnow.snow.test.apptest;

import java.util.Map;

import org.junit.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.util.MapUtil;

public class WebRestTest extends SnowTestSupport {

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp");
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
    public void testWebOptions(){
        RequestContextMock rc;
        Map result;

        rc = requestContextFactory.createRequestContext(RequestMethod.OPTIONS, "/api/echoParam1");
        rc.setParamMap(MapUtil.mapIt("param1", "123Hello"));
        webController.service(rc);
        result = rc.getResponseAsJson();

        Assert.assertEquals("optionsEchoParam1",result.get("method"));
    }

	@Test
	public void testWebPatch(){
		RequestContextMock rc;
		Map result;

		rc = requestContextFactory.createRequestContext(RequestMethod.PATCH, "/api/echoParam1");
		rc.setParamMap(MapUtil.mapIt("param1", "123Hello"));
		webController.service(rc);
		result = rc.getResponseAsJson();

		Assert.assertEquals("patchEchoParam1",result.get("method"));
	}
    
    @Test
    public void testWebPost(){
        RequestContextMock rc;
        Map result;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.POST, "/api/set-val1");
        rc.setParamMap(MapUtil.mapIt("val1", "123Hello"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        
        
        Assert.assertEquals("123Hello",result.get("val1"));        
    }
    
    @Test
    public void testWebPut(){
        RequestContextMock rc;
        Map result;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.PUT, "/api/put-key-value");
        rc.setParamMap(MapUtil.mapIt("key", "cat-01","value", "123"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        
        Assert.assertEquals("123",result.get("cat-01"));        
    }  
    
    
    @Test
    public void testWebDelete(){
        RequestContextMock rc;
        Map result;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.DELETE, "/api/delete-entity");
        rc.setParamMap(MapUtil.mapIt("entity_id","123"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        
        Assert.assertEquals(123,result.get("deleted_entity_id"));        
    }
    
    @Test
    public void testWebGetWithMethodName(){
        RequestContextMock rc;
        Map result;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/fromMethodName");
        rc.setParamMap(MapUtil.mapIt("info", "123Hello"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        Assert.assertEquals("123Hello",result.get("info-echo"));
    }    
    
    @Test
    public void testGetAndPost(){
        RequestContextMock rc;
        Map result;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/post-and-get");
        rc.setParamMap(MapUtil.mapIt("info", "123Hello"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        Assert.assertEquals("123Hello",result.get("info-echo"));
        Assert.assertEquals("GET", result.get("method-echo"));

        rc = requestContextFactory.createRequestContext(RequestMethod.POST, "/post-and-get");
        rc.setParamMap(MapUtil.mapIt("info", "123Hello"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        Assert.assertEquals("123Hello",result.get("info-echo"));
        Assert.assertEquals("POST", result.get("method-echo"));        
    }
    
    
    @Test
    public void testDeleteWithPathVar(){
        RequestContextMock rc;
        Map result;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.DELETE, "/delete-item-333-");
        webController.service(rc);
        result = rc.getResponseAsJson();
        Assert.assertEquals(333,result.get("deletedItemId"));
    }
    
}
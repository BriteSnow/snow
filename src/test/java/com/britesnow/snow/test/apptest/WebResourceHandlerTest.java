package com.britesnow.snow.test.apptest;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import com.britesnow.snow.testsupport.SnowWebApplicationTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;

public class WebResourceHandlerTest extends SnowWebApplicationTestSupport {
    
    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowWebApplicationTestSupport.initWebApplication("src/test/resources/simpleApp");
    }
    
    @Test
    public void testEcho1() throws Exception{
        String result;
        RequestContextMock rc;
        String fileName = "weclomeworld.txt";

        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/echo1/" + fileName);
        webController.service(rc);
        result = rc.getResponseAsString();
        assertEquals(fileName,result);
        
        fileName = "weclome.text";
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/echo1/" + fileName);
        webController.service(rc);
        result = rc.getResponseAsString();
        assertEquals(fileName,result);
    }
    
    @Test
    public void testEcho2() throws Exception{
        String result;
        RequestContextMock rc;
        String fileName = "weclomeworld.txt";

        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/echo2/" + fileName);
        webController.service(rc);
        result = rc.getResponseAsString();
        assertEquals(fileName,result);
        
        fileName = "weclome.text";
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/echo2/" + fileName);
        webController.service(rc);
        result = rc.getResponseAsString();
        assertEquals(fileName,result);
    }    

}

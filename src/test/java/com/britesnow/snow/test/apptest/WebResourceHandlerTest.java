package com.britesnow.snow.test.apptest;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import com.britesnow.snow.test.app.simpleapp.web.WebResourceHandlers;
import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.web.binding.WebClasses;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class WebResourceHandlerTest extends SnowTestSupport {
    
    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp", new CustomAppModule());
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

class CustomAppModule extends AbstractModule{

    @Override
    protected void configure() {
        
    }
    
    @Provides
    @WebClasses
    public Class[] provideWebClasses(){
        return new Class[] {WebResourceHandlers.class};
    }
    
}

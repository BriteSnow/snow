package com.britesnow.snow.test.apptest;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.util.MapUtil;

public class WebBundleTest extends SnowTestSupport {

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp");
    }
    
    @Test
    public void testJsWebBundlePage() throws Exception{
        String result;
        RequestContextMock rc;
        
        // test a regular get on a page and check if we have the right <script tags
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/webBundleTest");
        webController.service(rc);
        result = rc.getResponseAsString();
        String shouldContain = "<script type='text/javascript' src='/js/_web_bundle_all__";
        assertContains(new String[]{shouldContain},result);
        
        // test with _debug_links
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/webBundleTest");
        rc.setParamMap(MapUtil.mapIt("_debug_links","true"));
        webController.service(rc);
        result = rc.getResponseAsString();
        assertContains(new String[]{"js1.js","js2.js"},result);
    }
    
    @Test
    public void testJsWebBundleResult() throws Exception{
        String result;
        RequestContextMock rc;        
        
        // test the get on a _web_bundle_all__.... request.
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/js/_web_bundle_all__anykey__.js");
        webController.service(rc);
        result = rc.getResponseAsString();
        assertContains(new String[]{"var js1","var js2"},result);
    }
    
    @Test
    public void testLessWebBundlePage() throws Exception{
        String result;
        RequestContextMock rc;  
        
        // test the regular request (no _debug_links)
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/webBundleLessTest");
        webController.service(rc);
        result = rc.getResponseAsString();
        assertContains(new String[]{"<link type='text/css' href='/css/_web_bundle_all__",".less"},result);    
        
        // test with the _debug_links
        // Note: for now, the .less should not be split (since the less processor supports only one .less)
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/webBundleLessTest");
        rc.setParamMap(MapUtil.mapIt("_debug_links","true"));
        webController.service(rc);
        result = rc.getResponseAsString();
        assertContains(new String[]{"<link type='text/css' href='/css/_web_bundle_all__",".less"},result);

    }

    @Test
    public void testLessWebBundleResult() throws Exception{
        String result;
        RequestContextMock rc;        
        
        // test the get on a _web_bundle_all__....less
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/css/_web_bundle_all__anykey.less");
        webController.service(rc);
        result = rc.getResponseAsString();
        assertContains(new String[]{".less3",".less4"},result);
    }
    
    
    
}

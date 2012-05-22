package com.britesnow.snow.test.apptest;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;

public class PathInfoMatcherTest extends SnowTestSupport {

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp");
    }

    //@Test
    public void testPiIs() throws Exception {
        RequestContextMock rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/pathInfoMatcher");
        webController.service(rc);

        String result = rc.getResponseAsString();
        assertContains(new String[] { "isPathInfoMatcher:true", "isContactPage:false" }, result);
    }
    
    @Test
    public void testPiIsFolder() throws Exception {
        RequestContextMock rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/subfolder/");
        webController.service(rc);
        String result = rc.getResponseAsString();
        assertContains(new String[] { "isSubfoler:true"}, result);
        
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/subfolder/index");
        webController.service(rc);
        result = rc.getResponseAsString();
        assertContains(new String[] { "isSubfolerIndex:true"}, result);
    }    

    //@Test
    public void testPiStarts() throws Exception {
        RequestContextMock rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/subfolder/pathInfoMatcher");
        webController.service(rc);

        String result = rc.getResponseAsString();
        assertContains(new String[] { "startsSubfolder:true", "startsRoot:true", "startsContactPage:false" }, result);
    }
}

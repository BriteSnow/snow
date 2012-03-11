package com.britesnow.snow.test.apptest;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowWebApplicationTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;

public class PathInfoMatcherTest extends SnowWebApplicationTestSupport {

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowWebApplicationTestSupport.initWebApplication("src/test/resources/simpleApp");
    }

    @Test
    public void testPiIs() throws Exception {
        RequestContextMock rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/pathInfoMatcher");
            webController.service(rc);

            String result = rc.getResponseAsString();
            assertContains(new String[]{"isPathInfoMatcher:true","isContactPage:false"}, result);
    }
    
    
    @Test
    public void testPiStarts() throws Exception {
        RequestContextMock rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/subfolder/pathInfoMatcher");
            webController.service(rc);

            String result = rc.getResponseAsString();
            assertContains(new String[]{"startsSubfolder:true","startsRoot:true","startsContactPage:false"}, result);
    }    
}

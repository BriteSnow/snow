package com.britesnow.snow.test.apptest;

import static org.junit.Assert.assertEquals;



import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;

public class FreemarkerDirectiveTest extends SnowTestSupport {

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp");
    }
    
    @Test
    public void testFreemarkerMethod() throws Exception {
        String result;
        RequestContextMock rc;

        // test with the /freemarkerDirectiveTest path
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/freemarkerMethodTest");
        webController.service(rc);
        result = rc.getResponseAsString();
        
        // to change accordingly
        assertEquals("---hello from echoMethod---", result);
    }    


}


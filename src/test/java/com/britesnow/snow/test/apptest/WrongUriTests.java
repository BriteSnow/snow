package com.britesnow.snow.test.apptest;

import org.junit.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;

public class WrongUriTests extends SnowTestSupport {

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp");
    }
    
    //@Test
    public void getWrongTemplatePage(){
        RequestContextMock rc;
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/wrongTemplatePage");
        webController.service(rc);
        Assert.assertEquals(404,rc.getAbortException().getCode());
    }  
    
    @Test
    public void getWrongJsonPath(){
        RequestContextMock rc;
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/wrongJusonPath.json");
        webController.service(rc);
        Assert.assertEquals(404,rc.getAbortException().getCode());
    }     
}

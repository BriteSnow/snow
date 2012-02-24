package com.britesnow.snow.test.apptest;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowWebApplicationTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;

public class LessWebRequestTest  extends SnowWebApplicationTestSupport {
    
    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowWebApplicationTestSupport.initWebApplication("src/test/resources/simpleApp");
    }
    
    
    @Test
    public void testLessToCssWebRequest() throws Exception{
        String result;
        RequestContextMock rc;        
        
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/css/imports.less.css");
        webController.service(rc);
        result = rc.getResponseAsString().replaceAll("\n","");
        Assert.assertEquals(".colorMix {  color: #ffffff;}.some {  color: #ffffff;}.in-variables {  color: #ff0000;}.require-variables {  color: #ff0000;}", result);

        // doing twice to make sure the cache work (however, we do not really have a way to test if it is cached, probably, doing some timing would be good)
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/css/imports.less.css");
        webController.service(rc);
        result = rc.getResponseAsString().replaceAll("\n","");
        Assert.assertEquals(".colorMix {  color: #ffffff;}.some {  color: #ffffff;}.in-variables {  color: #ff0000;}.require-variables {  color: #ff0000;}", result);

        
        
    }    

}

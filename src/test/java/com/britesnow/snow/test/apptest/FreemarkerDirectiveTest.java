package com.britesnow.snow.test.apptest;

import static org.junit.Assert.assertEquals;



import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.test.app.simpleapp.web.FreemarkerHandlers;
import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.web.binding.WebClasses;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class FreemarkerDirectiveTest extends SnowTestSupport {

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp", new Custom2AppModule());
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

class Custom2AppModule extends AbstractModule{

    @Override
    protected void configure() {
        
    }
    
    @Provides
    @WebClasses
    public Class[] provideWebClasses(){
        return new Class[] {FreemarkerHandlers.class};
    }
    
}    


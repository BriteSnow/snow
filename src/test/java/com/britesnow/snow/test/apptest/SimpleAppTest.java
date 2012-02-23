package com.britesnow.snow.test.apptest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowWebApplicationTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.util.MapUtil;

public class SimpleAppTest extends SnowWebApplicationTestSupport {

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowWebApplicationTestSupport.initWebApplication("src/test/resources/simpleApp");
    }

    @Test
    public void testHelloPage() throws Exception {

            RequestContextMock rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/helloPage");
            Map<String, Object> paramMap = (Map<String, Object>) MapUtil.mapIt("name", "John");
            rc.setParamMap(paramMap);

            webController.service(rc);

            String result = rc.getResponseAsString();

            assertEquals("---Hello John---", result);
    }

    @Test
    public void testContactJson() throws Exception {
            Map result;
            RequestContextMock rc;

            // test getting contact id = 1 (Mike)
            rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/contact.json");
            rc.setParamMap(MapUtil.mapIt("id", "1"));
            webController.service(rc);
            result = rc.getResponseAsJson();
            assertEquals("Mike",MapUtil.getNestedValue(result, "contact.name"));

            // test getting contact id = 2 (Dylan)
            rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/contact.json");
            rc.setParamMap(MapUtil.mapIt("id", "2"));
            webController.service(rc);
            result = rc.getResponseAsJson();
            assertEquals("Dylan",MapUtil.getNestedValue(result, "contact.name"));

 
    }

    @Test
    public void testContactPage() {
        try {
            String result;
            RequestContextMock rc;
            
            // test with the /contact path
            rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/contact");
            rc.setParamMap(MapUtil.mapIt("id", "1"));
            webController.service(rc);
            result = rc.getResponseAsString();
            assertEquals("---Hello Mike---", result);
            
            // test with the /contact.ftl path 
            rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/contact.ftl");
            rc.setParamMap(MapUtil.mapIt("id", "1"));
            webController.service(rc);
            result = rc.getResponseAsString();
            assertEquals("---Hello Mike---", result);
            
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }
    
    @Test
    public void testNotesIndexPage() throws Exception {
            String result;
            RequestContextMock rc;
            
            // test with the /contact path
            rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/notes/");
            webController.service(rc);
            result = rc.getResponseAsString();
            
            assertEquals("---!!!notes/_frame!!!This is the notes/index.ftl page!!!/notes/_frame!!!---",result);
    }   
    
    @Test
    public void testAddContactAction() throws Exception {
            Map result;
            RequestContextMock rc;
            
            // test add contact
            rc = requestContextFactory.createRequestContext(RequestMethod.POST, "/_actionResponse.json");
            rc.setParamMap(MapUtil.mapIt("action","addContact","name", "Jennifer"));
            webController.service(rc);
            result = rc.getResponseAsJson();
            assertEquals(MapUtil.getNestedValue(result, "result.name"),"Jennifer");

            
            String newContactId = MapUtil.getNestedValue(result, "result.id");
            rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/contact.json");
            rc.setParamMap(MapUtil.mapIt("id",newContactId));
            webController.service(rc);
            result = rc.getResponseAsJson();
            assertEquals("Jennifer",MapUtil.getNestedValue(result, "contact.name"));


    }    
    
    @Test
    public void testWebBundle() throws Exception{
        String result;
        RequestContextMock rc;
        
        // test with the contact list
        rc = requestContextFactory.createRequestContext(RequestMethod.POST, "/webBundleTest");
        webController.service(rc);
        result = rc.getResponseAsString();
        String shouldContain = "<script type='text/javascript' src='/js/_web_bundle_all__";
        assertTrue("Should contain:\n" + shouldContain + " but was:\n" + result,result.contains(shouldContain) );
        
        // test with _debug_links
        rc = requestContextFactory.createRequestContext(RequestMethod.POST, "/webBundleTest");
        rc.setParamMap(MapUtil.mapIt("_debug_links","true"));
        webController.service(rc);
        result = rc.getResponseAsString();
        
        assertTrue("Should contain: 'js1.js' and 'js2.js' but was:\n" + result,result.contains("js1.js") && result.contains("js2.js")  );
        
    }

}

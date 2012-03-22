package com.britesnow.snow.test.apptest;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowWebApplicationTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.util.MapUtil;

public class SimpleWebHandlersTest extends SnowWebApplicationTestSupport {

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
    public void testBool() throws Exception {
        // ,"boolTrue","true","boolFalse","false"
        Map result;

        RequestContextMock rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/testBool.json");
        Map<String, Object> paramMap = (Map<String, Object>) MapUtil.mapIt("boolTrue", "true", "boolFalse", "false");
        rc.setParamMap(paramMap);

        webController.service(rc);

        result = rc.getResponseAsJson();
        
        assertEquals("true", result.get("boolTrue").toString());
        assertEquals("false", result.get("boolFalse").toString());
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
        assertEquals("Mike", MapUtil.getDeepValue(result, "contact.name"));

        // test getting contact id = 2 (Dylan)
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/contact.json");
        rc.setParamMap(MapUtil.mapIt("id", "2"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        assertEquals("Dylan", MapUtil.getDeepValue(result, "contact.name"));

    }

    @Test
    public void testContactPage() throws Exception {
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
    }

    @Test
    public void testNotesIndexPage() throws Exception {
        String result;
        RequestContextMock rc;

        // test with the /contact path
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/notes/");
        webController.service(rc);
        result = rc.getResponseAsString();

        assertEquals("---!!!notes/_frame!!!This is the notes/index.ftl page!!!/notes/_frame!!!---", result);
    }

    /**
     * The .do notation is recommended over the _actionResponse.json (which is deprecated)
     * 
     * @throws Exception
     */
    @Test
    public void testAddContactActionDo() throws Exception {
        Map result;
        RequestContextMock rc;

        // test add contact
        rc = requestContextFactory.createRequestContext(RequestMethod.POST, "/addContact.do");
        rc.setParamMap(MapUtil.mapIt("name", "Jennifer"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        assertEquals("Jennifer", result.get("name"));

        String newContactId = MapUtil.getDeepValue(result, "id");
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/contact.json");
        rc.setParamMap(MapUtil.mapIt("id", newContactId));
        webController.service(rc);
        result = rc.getResponseAsJson();
        assertEquals("Jennifer", MapUtil.getDeepValue(result, "contact.name"));
    }

    @Test
    public void testAddContactAction() throws Exception {
        Map result;
        RequestContextMock rc;

        // test add contact
        rc = requestContextFactory.createRequestContext(RequestMethod.POST, "/_actionResponse.json");
        rc.setParamMap(MapUtil.mapIt("action", "addContact", "name", "Jennifer"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        assertEquals("Jennifer", result.get("name"));

        String newContactId = MapUtil.getDeepValue(result, "id");
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/contact.json");
        rc.setParamMap(MapUtil.mapIt("id", newContactId));
        webController.service(rc);
        result = rc.getResponseAsJson();
        assertEquals("Jennifer", MapUtil.getDeepValue(result, "contact.name"));
    }

}

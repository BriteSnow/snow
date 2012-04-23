package com.britesnow.snow.test.apptest;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.util.MapUtil;

public class SystemWebParamResolversTest extends SnowTestSupport  {

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp");
    }
    
    @Test
    public void testHelloPage() throws Exception {

        RequestContextMock rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/testEnumWebParam.json");
        rc.setParamMap(MapUtil.mapIt("testenum","foo"));

        webController.service(rc);

        Map result = rc.getResponseAsJson();
        
        assertEquals("foo", result.get("result").toString());
    }
}

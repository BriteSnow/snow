package com.britesnow.snow.test.apptest;

import static org.junit.Assert.assertEquals;

import java.util.List;
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
    public void testEnum() throws Exception {

        RequestContextMock rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/testEnumWebParam.json");
        rc.setParamMap(MapUtil.mapIt("testenum","foo"));

        webController.service(rc);

        Map result = rc.getResponseAsJson();
        
        assertEquals("foo", result.get("result").toString());
    }
    
    @Test
    public void testLongArray() throws Exception {
        RequestContextMock rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/testLongArrayWebParam");
        String[] arrayLongs = {"1","2","3"};
        rc.setParamMap(MapUtil.mapIt("stringArrayLongs", arrayLongs));
        
        webController.service(rc);
        
        Map result = rc.getResponseAsJson();
        List values =  (List) result.get("stringArrayLongs");
        assertEquals(3,values.size());
        assertEquals(1,values.get(0));
        assertEquals(2,values.get(1));
        assertEquals(3,values.get(2));
        
    }
    
    
}

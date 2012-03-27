package com.britesnow.snow.test.apptest;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.util.MapUtil;

public class EmployeeWebHandlersTest extends SnowTestSupport{

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp");
    }
    
    @Test
    public void testEmployeeGet() throws Exception {
        Map result;
        RequestContextMock rc;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/employee.json");
        rc.setParamMap(MapUtil.mapIt("employeeId", "1"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        assertEquals("Mike",MapUtil.getDeepValue(result, "employee.firstName"));
        
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/employee.json");
        rc.setParamMap(MapUtil.mapIt("employeeId", "2"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        assertEquals("Dylan",MapUtil.getDeepValue(result, "employee.firstName"));
    }
    
    @Test
    public void testEmployeeAdd() throws Exception {
        Map result;
        RequestContextMock rc;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.POST, "/_actionResponse.json");
        rc.setParamMap(MapUtil.mapIt("action","addEmployee","firstName","Jennifer","lastName","kopel"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        assertEquals("Jennifer",MapUtil.getDeepValue(result, "firstName"));
        
        Long newId = MapUtil.getDeepValue(result, "id",Long.class);

        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/employee.json");
        rc.setParamMap(MapUtil.mapIt("employeeId", newId));
        webController.service(rc);
        result = rc.getResponseAsJson();
        assertEquals("Jennifer",MapUtil.getDeepValue(result, "employee.firstName"));
    }
    
    
    @Test
    public void testDaoException() throws Exception{
        String result;
        RequestContextMock rc;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/employee.json");
        rc.setParamMap(MapUtil.mapIt("employeeId", "-1"));
        webController.service(rc);
        result = rc.getResponseAsString();
        assertEquals("From WebExceptionCatchers.catchDaoException: ENTITY_NOT_FOUND Employee is null",result);
        
    }

}

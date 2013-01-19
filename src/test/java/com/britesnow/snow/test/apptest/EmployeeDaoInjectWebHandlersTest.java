package com.britesnow.snow.test.apptest;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.util.MapUtil;

public class EmployeeDaoInjectWebHandlersTest extends SnowTestSupport{

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp");
    }
    
    @Test
    public void testEmployeeGet() throws Exception {
        Map result;
        RequestContextMock rc;
        
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, "/employeeDaoInject.json");
        rc.setParamMap(MapUtil.mapIt("employeeId", "1"));
        webController.service(rc);
        result = rc.getResponseAsJson();
        assertEquals("Mike",MapUtil.getDeepValue(result, "employee.firstName"));
    }
    
}

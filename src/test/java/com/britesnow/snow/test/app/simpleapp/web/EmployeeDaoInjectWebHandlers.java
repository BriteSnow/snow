package com.britesnow.snow.test.app.simpleapp.web;

import java.util.Map;

import javax.inject.Singleton;

import com.britesnow.snow.test.app.simpleapp.dao.IDao;
import com.britesnow.snow.test.app.simpleapp.entity.Employee;
import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.param.annotation.WebModel;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.google.inject.Inject;

@Singleton
public class EmployeeDaoInjectWebHandlers {

    
    //@Inject
    //EmployeeDao employeeDao;
    @Inject
    IDao<Employee> employeeDao;
    
    
    @WebModelHandler(startsWith="/employeeDaoInject")
    public void employeeRequest(@WebModel Map m, @WebParam("employeeId")Long employeeId ){
        Employee employee = employeeDao.get(employeeId);
        
        m.put("employee",employee);
    }
    
}

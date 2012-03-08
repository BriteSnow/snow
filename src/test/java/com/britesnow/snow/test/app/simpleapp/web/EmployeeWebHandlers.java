package com.britesnow.snow.test.app.simpleapp.web;

import java.util.Map;

import com.britesnow.snow.test.app.simpleapp.dao.EmployeeDao;
import com.britesnow.snow.test.app.simpleapp.entity.Employee;
import com.britesnow.snow.web.handler.annotation.WebActionHandler;
import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.param.annotation.WebModel;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.google.inject.Inject;

public class EmployeeWebHandlers {

    @Inject
    EmployeeDao employeeDao;
    
    @WebModelHandler(startsWith="/employee")
    public void employeeRequest(@WebModel Map m, @WebParam("employeeId")Employee employee ){
        m.put("employee",employee);
    }
    
    
    @WebActionHandler
    public Employee addEmployee(@WebParam("firstName")String firstName, @WebParam("lastName")String lastName){
        return employeeDao.createNewEmployee(firstName, lastName);
    }
}

package com.britesnow.snow.test.app.simpleapp.dao;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.britesnow.snow.test.app.simpleapp.entity.Employee;

@Singleton
public class EmployeeDao implements IDao<Employee> {

    private Map<Long,Employee> employeStore = new HashMap<Long, Employee>();
    
    private Long idSeq = 1L;

    public EmployeeDao(){
        createNewEmployee("Mike","Donavan");
        createNewEmployee("Dylan","Brown");
        
    }
    
    public Employee createNewEmployee(String firstName, String lastName){
      if (firstName == null){
            throw new DaoException(DaoException.Error.ENTITY_NOT_FOUND,"First Names Cannot be null");
        }
        
        Employee emp = new Employee();
        
        emp.setId(idSeq);
        emp.setFirstName(firstName);
        emp.setLastName(lastName);
        employeStore.put(idSeq,emp);
        idSeq++;   

        return emp;
    }
    
    public Employee get(Long id){
        Employee employee = employeStore.get(id);
        if (employee == null){
            throw new DaoException(DaoException.Error.ENTITY_NOT_FOUND,"Employee is null");
        }
        
        return employee;
    }
    
}

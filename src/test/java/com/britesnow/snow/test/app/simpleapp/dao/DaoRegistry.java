package com.britesnow.snow.test.app.simpleapp.dao;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.britesnow.snow.test.app.simpleapp.entity.BaseEntity;
import com.britesnow.snow.test.app.simpleapp.entity.Employee;
import com.britesnow.snow.web.binding.EntityClasses;
import com.google.inject.Injector;

@Singleton
public class DaoRegistry {

    public Map<Class<? extends BaseEntity>,IDao> daoByEntity = new HashMap<Class<? extends BaseEntity>, IDao>();
    

    @Inject
    public void init(Injector injector, @EntityClasses Class[] entityClasses){
        // for this test simple app, just hardcode the daoByEntity registry 
        // but usually, we would go through the .dao.* package and get the generic type of the Dao
        // to match with each entity class, and instanciate a GenericDao for the one that do not have a specific DAO.
        daoByEntity.put(Employee.class, injector.getInstance(EmployeeDao.class));
    }
    
    public <E> IDao<E> getDao(Class<E> cls){
        return daoByEntity.get(cls);
    }
}

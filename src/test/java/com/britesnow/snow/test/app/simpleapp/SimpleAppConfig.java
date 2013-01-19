package com.britesnow.snow.test.app.simpleapp;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.inject.Inject;

import com.britesnow.snow.test.app.simpleapp.dao.DaoRegistry;
import com.britesnow.snow.test.app.simpleapp.dao.IDao;
import com.britesnow.snow.test.app.simpleapp.entity.Employee;
import com.britesnow.snow.web.binding.EntityClasses;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;


public class SimpleAppConfig extends AbstractModule {
    @Override
    protected void configure() {
        //bind(new TypeLiteral<IDao<Employee>>() {}).to(EmployeeDao.class);
        bindDao(Employee.class);
    }


    @Provides
    @EntityClasses
    public Class[] providesEntityClasses(){
        return new Class[]{Employee.class};
    }
    
    
    private <T> void bindDao(final Class entityClass){
        //bind(TypeLiteral.get(iDaoEmployee)).to( (Class<? extends IDao>) EmployeeDao.class);
        Type daoParamType = new ParameterizedType() {
            public Type getRawType() {
                return IDao.class;
            }

            public Type getOwnerType() {
                return null;
            }

            public Type[] getActualTypeArguments() {
                return new Type[] {entityClass};
            }
        };        
        
        DaoProvider daoProvider = new DaoProvider(entityClass);
        requestInjection(daoProvider);
        bind(TypeLiteral.get(daoParamType)).toProvider(daoProvider);
    }
    
}


class DaoProvider implements Provider{
    
    private Class entityClass;
    
    @Inject
    private DaoRegistry daoRegistry;

    public DaoProvider(Class entityClass){
        this.entityClass = entityClass;
    }

    @Override
    public Object get() {
        return daoRegistry.getDao(entityClass);
    }
    
}


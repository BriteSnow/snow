package com.britesnow.snow.web.db.hibernate;

import com.google.inject.AbstractModule;


/**
 * Default HibernateModule providing the following bindings: 
 * 
 * - HibernateSessionFactoryBuilder
 * - HibernateDaoHelper
 * - HibernateSessionInViewHandler
 * 
 * @author jeremychone
 *
 */
public class DefaultHibernateModule extends AbstractModule{

    public DefaultHibernateModule(){
    }
    
    
    @Override
    protected void configure() {
        
        bind(HibernateSessionFactoryBuilder.class).to(DefaultHibernateSessionFactoryBuilder.class);
        
        bind(HibernateDaoHelper.class).to(HibernateDaoHelperImpl.class);
        
        bind(HibernateSessionInViewHandler.class).to(DefaultHibernateSessionInViewHandler.class);
    }
    

}

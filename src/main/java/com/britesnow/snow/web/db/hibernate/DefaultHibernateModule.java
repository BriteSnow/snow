package com.britesnow.snow.web.db.hibernate;

import com.britesnow.snow.web.db.hibernate.annotation.Transactional;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;


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
        
        // bind the transactionalInterceptor
        TransactionalInterceptor transactionInterceptor = new TransactionalInterceptor();
        requestInjection(transactionInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class),transactionInterceptor);
    }
    

}

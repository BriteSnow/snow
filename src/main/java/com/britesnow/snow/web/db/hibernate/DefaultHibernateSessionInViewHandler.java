package com.britesnow.snow.web.db.hibernate;

import javax.inject.Singleton;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@Singleton
public class DefaultHibernateSessionInViewHandler implements HibernateSessionInViewHandler {
    static private Logger logger = LoggerFactory.getLogger(DefaultHibernateSessionInViewHandler.class);
    
    
    @Inject
    private HibernateSessionFactoryBuilder sessionFactoryBuilder;

    private FlushMode flushMode = FlushMode.AUTO;
    
    @Inject(optional=true)
    public void injectFlushMode(@Named("snow.hibernate.flushMode")String flushModeStr){
        if (flushModeStr != null) {
            FlushMode flushMode = FlushMode.valueOf(flushModeStr.toUpperCase());
            if(flushMode != null) {
                this.flushMode = flushMode;
            }
            else {
                logger.warn("unable to parse flush mode property value '" + flushModeStr + "'.  will default to " + flushMode);
            }
        }        
    }
    
    
    
    @Override
    public void openSessionInView() {
        closeSessionInView();
        SessionFactory sessionFactory = sessionFactoryBuilder.getSessionFactory();
        SessionHolder sessionHolder = new SessionHolder(sessionFactory, flushMode);
        SessionHolder.setThreadSessionHolder(sessionHolder);        
    }

    @Override
    public void afterActionProcessing() {
        // Do nothing in the default implementation
        
    }

    @Override
    public void closeSessionInView() {
        SessionHolder sessionHolder = SessionHolder.getThreadSessionHolder();
        if (sessionHolder != null) {
            sessionHolder.close();
        }
        SessionHolder.removeThreadSessionHolder();
    }

}

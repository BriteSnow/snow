package com.britesnow.snow.web.db.hibernate;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionalInterceptor implements MethodInterceptor {

    static private Logger logger = LoggerFactory.getLogger(TransactionalInterceptor.class);
    
    @Override
    public Object invoke(MethodInvocation invoc) throws Throwable {
        //get the sessionHolder
        SessionHolder sessionHolder = SessionHolder.getThreadSessionHolder();

        boolean txOwner = false;
        //// beginTransaction if necessary
        //if there is no transaction open, then, begin the transaction, 
        //and set this call as the txOwner
        // 2010-10-21-Jeremy: For now, if the sessionHolder is not found (unitTesting), we just ignore
        
        if (sessionHolder != null && !sessionHolder.isTxOpen()) {
            logger.debug("..... Transaction Start");
            sessionHolder.beginTransaction();
            txOwner = true;
        }

        Object ret = null;
        try {
            
            
            //// proceed to call
            ret = invoc.proceed();
            //// if this call had begun the transaction, then commit it
            if (sessionHolder != null && txOwner) {
                sessionHolder.commitTransaction();
                logger.debug(".......... Transaction Commit");
            }

        } catch (Throwable t) {
            
            t.printStackTrace();
            //SnowHibernateException she = new SnowHibernateException(t);
            System.out.flush();

            //// if this call had begun the transaction, then 
            //   do a rollback on exception
            if (sessionHolder != null && txOwner) {
                sessionHolder.rollbackTransaction();
                if (t instanceof SnowHibernateException){
                	logger.error("SnowHibernateException message: " + t.getMessage(),t);
				}
            }
            
            //Since we do not require the @Transactional methods to throws exception, we must throw runtime exception.
            if (t instanceof RuntimeException){
                throw t;
            }else{
                throw new RuntimeException(t);
            }
        }
        
        
        return ret;
    }

}

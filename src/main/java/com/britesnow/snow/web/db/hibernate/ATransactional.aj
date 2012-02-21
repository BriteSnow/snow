/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.db.hibernate;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.britesnow.snow.web.WebController;
import com.britesnow.snow.web.db.hibernate.annotation.Transactional;


public aspect ATransactional {

    /*
     * Matches the execution of any public method in a type with the
     * Transactional annotation, or any subtype of a type with the Transactional
     * annotation.
     * SEE: http://fisheye1.atlassian.com/browse/springframework/spring/aspectj/src/org/springframework/transaction/aspectj/AnnotationTransactionAspect.aj?r=HEAD
      private pointcut executionOfAnyPublicMethodInAtTransactionalType() :
         execution(public * ((@Transactional *)+).*(..)) && @this(Transactional);
         
     */
	static private Logger logger = LoggerFactory.getLogger(WebController.class);
	
    /**
     * The execution of any method that has the @Tx annotation
     */
    
    pointcut transactionalMethodExecution(Transactional transactional) :
        execution(* *(..)) && @annotation(transactional);

    /**
     * Implementating the transaction policies  
     */    
    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around(Transactional transactional) : transactionalMethodExecution(transactional) {
    	
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
            //System.out.println("..... before");
            ret = proceed(transactional);
            //System.out.println("..... after");
            //// if this call had begun the transaction, then commit it
            if (sessionHolder != null && txOwner) {
                sessionHolder.commitTransaction();
                logger.debug(".......... Transaction Commit");
                //System.out.println(".......... Transaction Commit");
            }

        } catch (Throwable t) {
            
            t.printStackTrace();
            //SnowHibernateException she = new SnowHibernateException(t);
            System.out.flush();
            //System.out.println("SnowHibernateException message: " + she.getMessage());
           
            //// if this call had begun the transaction, then 
            //   do a rollback on exception
            if (sessionHolder != null && txOwner) {
                sessionHolder.rollbackTransaction();
                if (t instanceof SnowHibernateException)
                logger.error("SnowHibernateException message: " + t.getMessage());
            }
            
            //Since we do not require the @Transactional methods to throws exception, we must throw runtime exception.
            if (t instanceof RuntimeException){
                throw ((RuntimeException)t);
            }else{
                throw new RuntimeException(t);
            }
        }
        
        
        return ret;
    }
}

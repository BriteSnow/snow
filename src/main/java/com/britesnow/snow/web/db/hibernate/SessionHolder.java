/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.db.hibernate;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class SessionHolder {

    static private ThreadLocal<SessionHolder> sessionHolderTl = new ThreadLocal<SessionHolder>();

    /*--------- ThreadLocal ---------*/
    static public SessionHolder getThreadSessionHolder() {
        return sessionHolderTl.get();
    }

    static public SessionHolder setThreadSessionHolder(SessionHolder sessionHolder) {
        sessionHolderTl.set(sessionHolder);
        return sessionHolder;
    }

    static public void removeThreadSessionHolder() {
        sessionHolderTl.remove();
    }
    /*--------- /ThreadLocal ---------*/

    private SessionFactory sessionFactory;

    //build on demand
    private Session        defaultSession;

    //created on demand
    private Session        txSession; //so far not used
    private boolean        txOpen = false;
    private FlushMode      flushMode;

    public SessionHolder(SessionFactory sessionFactory, FlushMode flushMode) {
        this.sessionFactory = sessionFactory;
        this.flushMode = flushMode;
    }

    public void flush() {
        if (defaultSession != null) {
            if (defaultSession.isOpen()) {
                defaultSession.flush();
    
    
            }
        }
        if (txSession != null) {
            if (txSession.isOpen()) {
                txSession.flush();
            }
        }

    }
    
    public void close() {
        if (defaultSession != null) {
            if (defaultSession.isOpen()) {
                defaultSession.close();
            }
        }
        if (txSession != null) {
            if (txSession.isOpen()) {
                txSession.close();
            }
        }

    }
    

    public Session getSession() {
        //if a transaction is open, they return the txSession
        if (txOpen) {
            return getTxSession();
        } else {
            return getDefaultSession();
        }
    }

    /*--------- Tx ---------*/

    public boolean isTxOpen() {
        return txOpen;
    }

    public Transaction beginTransaction() {
        //if the transaction is already open, throw an exception
        if (txOpen) {
            throw new RuntimeException("Cannot start transaction. Transaction already started.");
        }
        Session txSession = getTxSession();
        Transaction tx = txSession.beginTransaction();
        txOpen = true;
        return tx;
    }

    public void commitTransaction() {
        //if the transaction is not open, throw an exception
        if (!txOpen) {
            throw new RuntimeException("Cannot commit transaction. Transaction closed or not started.");
        }
        Session txSession = getTxSession();
        Transaction tx = txSession.getTransaction();
        tx.commit();
        txOpen = false;
    }

    public void rollbackTransaction() {
        //if the transaction is not open, throw an exception
        if (!txOpen) {
            throw new RuntimeException("Cannot rollback transaction. Transaction closed or not started.");
        }
        Session txSession = getTxSession();
        Transaction tx = txSession.getTransaction();
        tx.rollback();
        txOpen = false;
    }

    /*--------- /Tx ---------*/

    /*--------- Private Session Factories ---------*/
    private Session getDefaultSession() {
        if (defaultSession == null) {
            defaultSession = sessionFactory.openSession();
            defaultSession.setFlushMode(flushMode);
        }
        return defaultSession;
    }

    private Session getTxSession() {
        
        //for now, default to defaultSession
        ////NOTE: Hibernate does not really support 2 session when updating collections. Need to find what is the right way.
        return getDefaultSession();
        
        
        /**
        if (txSession == null) {
            Session readOnlysession = getReadOnlySession();
            txSession = sessionFactory.openSession(readOnlysession.connection());
        }
        return txSession;
        */
    }
    /*--------- /Private Session Factories ---------*/

}

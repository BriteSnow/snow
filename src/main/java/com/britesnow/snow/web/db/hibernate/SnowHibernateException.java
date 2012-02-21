package com.britesnow.snow.web.db.hibernate;

import java.sql.SQLException;

import org.hibernate.JDBCException;

import com.britesnow.snow.SnowRuntimeException;

public class SnowHibernateException extends SnowRuntimeException {

    public SnowHibernateException(Throwable t) {
        super(t);
        
        Throwable t2 = t;
        
        //try to get the root cause of the sqlException of it is a grammarException
        if (t2 instanceof JDBCException){
            t2 = ((JDBCException)t2).getSQLException();
            if (t2 != null){
                t2 = ((SQLException)t2).getNextException();
            }
        }
        
        //if we have a t2, then, override the cause
        usefulCause = t2;
        
        
        // TODO Auto-generated constructor stub
    }

    /**
     * 
     */
    private static final long serialVersionUID = -2009176985114280369L;

}

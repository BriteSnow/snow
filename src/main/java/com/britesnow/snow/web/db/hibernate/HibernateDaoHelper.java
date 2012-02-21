/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.db.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;

import java.util.List;


import org.hibernate.Session;



/**
 * 
 * Helper Interface to access Hibernate Session functions. 
 * 
 * @author Jeremy Chone
 * @date Jul 30, 2009
 */
public interface HibernateDaoHelper {


    public <T> T get(Class<T> cls, Serializable id);


    public <T> T save(T entity);


    public <T> T update(T entity);


    public void saveEntities(List entities);


    public void saveEntities(Object... entities);

    public void delete(Class cls, Serializable id);

    public void delete(Object entity);

    public void deleteEntities(List entities);

    public Object findFirst(String query, Object... values);

    public List<? extends Object> find(int pageIdx, int pageSize, String query, Object... values);
    
    public Integer executeHql(String query,Object... values);

    public <T> T reload(T obj, Serializable id);

    public void evict(Object object);

    public void flush();

    /**
     * Flush and clear the current session. <br>
     * NOTE: a current session need to be present in the View (i.e. Thread)
     * otherwise a RuntimeException will arise.
     */
    public void flushAndClear();

    /*--------- JDBC CALLS ---------*/
    public ResultSet executeSql(String sql, Object... args);

    public Session getSession();

    public Connection getConnection();


}

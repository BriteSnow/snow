/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.db.hibernate;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Session;

public class HibernateDaoHelperProxy implements HibernateDaoHelper{

    private HibernateDaoHelper daoHelper;

    public HibernateDaoHelperProxy(HibernateDaoHelper daoHelper) {
        this.daoHelper = daoHelper;
    }


    // --------- Proxied Method --------- //
    @Override
    public void delete(Class cls, Serializable id) {
        daoHelper.delete(cls,id);
    }

    @Override
    public void delete(Object entity) {
        daoHelper.delete(entity);
    }

    @Override
    public void deleteEntities(List entities) {
        daoHelper.deleteEntities(entities);        
    }

    @Override
    public void evict(Object object) {
        daoHelper.evict(object);
    }

    
    
    @Override
    public Integer executeHql(String query, Object... values) {
        return daoHelper.executeHql(query, values);
    }



    @Override
    public List<? extends Object> find(int pageIdx, int pageSize, String query, Object... values) {
        return daoHelper.find(pageIdx, pageSize, query, values);
    }

    @Override
    public Object findFirst(String query, Object... values) {
        return daoHelper.findFirst(query, values);
    }

    @Override
    public void flush() {
        daoHelper.flush();
        
    }

    @Override
    public void flushAndClear() {
        daoHelper.flushAndClear();
    }

    @Override
    public <T> T get(Class<T> cls, Serializable id) {
        return daoHelper.get(cls,id);
    }

    @Override
    public <T> T reload(T obj, Serializable id) {
        return daoHelper.reload(obj, id);
    }

    @Override
    public <T> T save(T entity) {
        return daoHelper.save(entity);
    }

    @Override
    public void saveEntities(List entities) {
        daoHelper.saveEntities(entities);
    }

    @Override
    public void saveEntities(Object... entities) {
        daoHelper.saveEntities(entities);
        
    }

    @Override
    public <T> T update(T entity) {
        return daoHelper.update(entity);
    }
    
    @Override
    public Session getSession() {
        return daoHelper.getSession();
    }    
    // --------- /Proxied Method --------- //
}

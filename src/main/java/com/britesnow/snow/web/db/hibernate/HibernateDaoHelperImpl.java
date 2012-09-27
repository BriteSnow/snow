/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.db.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;

import com.britesnow.snow.web.db.hibernate.annotation.Transactional;
import com.google.inject.Singleton;

/**
 * 
 * Helper Class to access Hibernate Session functions. Note that this class uses
 * SessionHolder and assumes that HibernateHandler.openSessionInView has been
 * called.
 * 
 * @author Jeremy Chone
 * @date Jul 30, 2009
 */
@Singleton
public class HibernateDaoHelperImpl implements HibernateDaoHelper {

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#get(java.lang.Class, java.io.Serializable)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> cls, Serializable id) {
        try {
            if (cls != null && id != null) {
                return (T) getSession().get(cls, id);
            } else {
                return null;
            }
        } catch (Throwable e) {            
            throw new SnowHibernateException(e);
        }

    }

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#save(T)
     */
    @Transactional
    public <T> T save(T entity) {

        Session session = getSession();
        try {
            session.save(entity);
        } catch (Throwable e) {
            throw new SnowHibernateException(e);
        }

        return entity;
    }

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#update(T)
     */
    @Transactional
    public <T> T update(T entity) {
        Session session = getSession();
        try {
            session.update(entity);
        } catch (Throwable e) {
            throw new SnowHibernateException(e);
        }
        return entity;
    }

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#saveEntities(java.util.List)
     */
    @Transactional
    public void saveEntities(List entities) {
        for (Object entity : entities) {
            save(entity);
        }
    }

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#saveEntities(java.lang.Object)
     */
    @Transactional
    public void saveEntities(Object... entities) {
        for (Object entity : entities) {
            save(entity);
        }
    }

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#delete(java.lang.Class, java.io.Serializable)
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public void delete(Class cls, Serializable id) {
        if (id != null) {
            Object entity = get(cls, id);
            delete(entity);
        }

    }

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#delete(java.lang.Object)
     */
    @Transactional
    public void delete(Object entity) {
        if (entity != null) {
            try {
                getSession().delete(entity);
            } catch (Throwable e) {
                throw new SnowHibernateException(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#deleteEntities(java.util.List)
     */
    @Transactional
    public void deleteEntities(List entities) {
        if (entities != null) {
            for (Object entity : entities) {
                delete(entity);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#findFirst(java.lang.String, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Object findFirst(String query, Object... values) {
        List<Object> results = (List<Object>) find(0, 1, query, values);
        if (results != null && results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }

    }


    /**
     * For now, this is transactional to support Postgres (if the query fail then postgres lock all the ss
     * @see com.britesnow.snow.web.db.hibernate.HibernateDaoHelper#find(int, int, java.lang.String, java.lang.Object[])
     */
    @SuppressWarnings("unchecked")
    public List<? extends Object> find(int pageIdx, int pageSize, String query, Object... values) {
        Session session = getSession();
        try {
            Query q = session.createQuery(query);

            if (values != null) {
                int i = 0;
                for (Object val : values) {
                    q.setParameter(i++, val);
                }
            }
            q.setFirstResult(pageIdx * pageSize);
            q.setMaxResults(pageSize);

            return q.list();
        } catch (Throwable e) {
            throw new SnowHibernateException(e);
        }
    }

    public Integer executeHql(String query, Object... values) {
        if (query != null) {
            Session session = getSession();
            try {
                Query q = session.createQuery(query);
                if (values != null) {
                    int i = 0;
                    for (Object val : values) {
                        q.setParameter(i++, val);
                    }
                }
                return q.executeUpdate();
            } catch (Throwable e) {
                throw new SnowHibernateException(e);
            }
        } else {
            return null;
        }
    }

    /*--------- Session Refresh ---------*/

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#reload(T, java.io.Serializable)
     */
    @SuppressWarnings("unchecked")
    public <T> T reload(T obj, Serializable id) {
        if (obj != null && id != null) {
            try {
                evict(obj);
                return (T) get(obj.getClass(), id);
            } catch (Throwable e) {
                throw new SnowHibernateException(e);
            }
        } else {
            return null;
        }

    }

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#evict(java.lang.Object)
     */
    public void evict(Object object) {
        if (object != null) {
            try {
                getSession().evict(object);
            } catch (Throwable e) {
                throw new SnowHibernateException(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#flush()
     */
    public void flush() {
        Session session = getSession();
        try {

            session.flush();
        } catch (Throwable e) {
            throw new SnowHibernateException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#flushAndClear()
     */
    public void flushAndClear() {
        Session session = getSession();
        try {
            session.flush();
            session.clear();
        } catch (Throwable e) {
            throw new SnowHibernateException(e);
        }
    }

    /*--------- /Session Refresh ---------*/

    /*--------- JDBC CALLS ---------*/
    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#executeSql(java.lang.String)
     */
    public ResultSet executeSql(String sql, Object... args) {
        try {
            ResultSet rs = null;
            Connection con = getConnection();
            if (args != null && args.length > 0) {
                PreparedStatement stmt = con.prepareStatement(sql);
                int num = 1;
                for (Object arg : args) {
                    stmt.setObject(num, arg);
                    num++;
                }
                rs = stmt.executeQuery();
            } else {

                Statement statement = con.createStatement();
                statement.execute(sql);
                con.commit();
                rs = statement.getResultSet();
            }

            return rs;
        } catch (Exception e) {
            throw new SnowHibernateException(e);
        }
    }

    /*--------- /JDBC CALLS ---------*/

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#getSession()
     */
    public Session getSession() {
        SessionHolder sessionHolder = SessionHolder.getThreadSessionHolder();
        if (sessionHolder == null) {
            throw new RuntimeException("Cannot get session. No SessionHolder bound to thread. "
                                    + "Call OpenSessionInView() before getting a session");
        }
        return sessionHolder.getSession();
    }

    /* (non-Javadoc)
     * @see org.snowfk.web.db.hibernate.HibernateDaoHelper#getConnection()
     */
    @SuppressWarnings("deprecation")
    public Connection getConnection() {
        try {
            ConnectionProvider cp =((SessionFactoryImplementor)getSession().getSessionFactory()).getConnectionProvider();  
            return cp.getConnection();    
        } catch (Exception e) {
            e.printStackTrace();
        }
          return null;
//        return getSession().connection();
    }

}

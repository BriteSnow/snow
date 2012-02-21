package com.britesnow.snow.web.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Convenient Object to create a HQL or SQL query with its associated parameters. 
 * 
 * This is optimized to not be threadSafe, so, use it in a single thread. 
 * 
 * Also, all setters/adders are chainable, you you can do queryBuilder.append("from User").append(" where name = ? ",userName) 
 * 
 * @author jeremychone
 *
 */
public class QueryBuilder {

    private List<String> stringList = new ArrayList<String>();
    private List<Object> paramList  = new ArrayList<Object>();

    public QueryBuilder() {
    }

    public QueryBuilder(String query, Object... params) {
        append(query, params);
    }

    public QueryBuilder append(String query, Object... params) {
        if (query != null) {
            stringList.add(query);
        }
        appendParams(params);
        return this;
    }

    public QueryBuilder prepend(String query, Object... params) {
        if (query != null) {
            stringList.add(0, query);
        }
        prependParams(params);
        return this;
    }

    public QueryBuilder appendParams(Object... params) {
        if (params != null) {
            paramList.addAll(Arrays.asList(params));
        }
        return this;
    }

    public QueryBuilder prependParams(Object... params) {
        if (params != null) {
            paramList.addAll(0, Arrays.asList(params));
        }
        return this;
    }

    public String toQuery() {
        StringBuilder query = new StringBuilder();

        for (String queryItem : stringList) {
            query.append(queryItem);
        }

        return query.toString();
    }

    public Object[] toParams() {
        return paramList.toArray();
    }

}

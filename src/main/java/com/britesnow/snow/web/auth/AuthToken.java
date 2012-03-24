/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.auth;

import java.util.HashMap;
import java.util.Map;

public class AuthToken<T> {

    public enum Type {
        root, admin, user, visitor;
    }

    private Type                type       = Type.visitor;
    private T                   user;
    private Map<String, String> groupNames = new HashMap<String, String>();

    public AuthToken() {

    }

    public AuthToken(Type type) {
        setType(type);
    }

    public boolean belongTo(String groupName) {
        return groupNames.containsKey(groupName);
    }

    /*--------- Getters ---------*/
    public boolean getHasRootRights() {
        return (type == Type.root) ? true : false;
    }

    public boolean getHasAdminRights() {
        switch (type) {
            case root:
            case admin:
                return true;
            default:
                return false;
        }
    }

    public boolean getHasUserRights() {
        switch (type) {
            case root:
            case admin:
            case user:
                return true;
            default:
                return false;
        }
    }
    public boolean getHasVisitorRights() {
        return true; //for now, everybody is at least visitor right
    }
    /*--------- /Getters ---------*/

    /* --------- Getters & Setters --------- */
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
        if (type != null) {
            groupNames.put(type.name(), type.name());

            //add the "admin" group if it is a root Auth type
            switch (type) {
                case root:
                case admin:
                    groupNames.put("admin", "admin");
                    break;

            }
        }

    }

    public T getUser() {
        return user;
    }

    public void setUser(T user) {
        this.user = user;
    }

    /* --------- /Getters & Setters --------- */

}

/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.param;

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.param.annotation.WebModel;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.britesnow.snow.web.param.annotation.WebPath;
import com.britesnow.snow.web.param.annotation.WebUser;

public class OldWebParamRef {

    private WebParam  webParam  = null;
    private WebUser   webUser   = null;
    private WebPath   webPath   = null;
    private WebModel    webMap    = null;


    private Class     paramClass;

    public OldWebParamRef(Class paramClass) {
        this.paramClass = paramClass;
    }

    public OldWebParamRef(WebParam webParam, Class paramClass) {
        this(paramClass);
        this.webParam = webParam;
    }

    public OldWebParamRef(WebUser webUser, Class paramClass) {
        this(paramClass);
        this.webUser = webUser;
    }

    public OldWebParamRef(WebPath webPath, Class paramClass) {
        this(paramClass);
        this.webPath = webPath;
    }

    public OldWebParamRef(WebModel webMap, Class paramClass) {
        this(paramClass);
        this.webMap = webMap;
    }

    public Class getArgClass(){
    	return paramClass;
    }
    @SuppressWarnings("unchecked")
    public Object getValue(Method m, RequestContext rc) {
        Object value = null;
        if (paramClass == RequestContext.class) {
            value = rc;
        } else if (paramClass == HttpServletRequest.class) {
            value = rc.getReq();
        } else if (paramClass == HttpServletResponse.class) {
            value = rc.getRes();
        } else if (paramClass == ServletContext.class) {
            value = rc.getServletContext();
        } else if (webParam != null && paramClass == Map.class) {
            value = rc.getParamMap(webParam.value());
        } else if (webMap != null) {
            value = rc.getWebModel();
        } else if (webUser != null) {
            if (rc.getAuth() != null) {
                value = rc.getAuth().getUser();
            }
        } else if (webPath != null) {
            //if the index has been set, then, return the single Path and convert to the appropriate type.
            if (webPath.value() > -1) {
                value = rc.getResourcePathAt(webPath.value(), paramClass, null);
            }
            //otherwise, return the full path
            else {
                value = rc.getResourcePath();
            }
        } else {
            String paramName;
            if (webParam != null) {
                paramName = webParam.value();
            } else {
                paramName = paramClass.getSimpleName();
                //lowercase the first char
                paramName = paramName.substring(0, 1).toLowerCase() + paramName.substring(1);
            }

            value = rc.getParam(paramName, paramClass);
        }

        return value;
    }

}

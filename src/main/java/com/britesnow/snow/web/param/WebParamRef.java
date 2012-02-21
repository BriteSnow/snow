/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.param.annotation.WebMap;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.britesnow.snow.web.param.annotation.WebPath;
import com.britesnow.snow.web.param.annotation.WebUser;

public class WebParamRef {

    private WebParam  webParam  = null;
    private WebUser   webUser   = null;
    private WebPath   webPath   = null;
    private WebMap    webMap    = null;

    private WebParameterParser webParameterParser;
    private Annotation webParameterAnnotation;

    private Class     paramClass;

    public WebParamRef(Class paramClass) {
        this.paramClass = paramClass;
    }

    public WebParamRef(WebParam webParam, Class paramClass) {
        this(paramClass);
        this.webParam = webParam;
    }

    public WebParamRef(WebUser webUser, Class paramClass) {
        this(paramClass);
        this.webUser = webUser;
    }

    public WebParamRef(WebPath webPath, Class paramClass) {
        this(paramClass);
        this.webPath = webPath;
    }

    public WebParamRef(WebMap webMap, Class paramClass) {
        this(paramClass);
        this.webMap = webMap;
    }

    public WebParamRef(WebParameterParser webParameterParser, Annotation webParameterAnnotation, Class paramClass) {
        this.webParameterParser = webParameterParser;
        this.webParameterAnnotation = webParameterAnnotation;
        this.paramClass = paramClass;
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
            value = rc.getWebMap();
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
        } else if (webParameterParser != null) {
            value = webParameterParser.getParameterValue(m, webParameterAnnotation, paramClass, rc);
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

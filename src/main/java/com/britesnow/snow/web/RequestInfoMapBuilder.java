/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.britesnow.snow.web.auth.Auth;

class RequestInfoMapBuilder {

    private static final String MODEL_KEY_REQUEST_CONTEXT = "rc";

    //requestMap
    private static final String MODEL_KEY_CONTEXT_PATH    = "contextPath";
    private static final String MODEL_KEY_PATH_INFO       = "pathInfo";
    private static final String MODEL_KEY_FULL_PATH       = "fullPath";
    private static final String MODEL_KEY_QUERY_STRING    = "queryString";
    private static final String MODEL_KEY_HREF            = "href";
    private static final String MODEL_KEY_PARAMS          = "param";
    private static final String MODEL_KEY_COOKIES         = "cookie";
    private static final String MODEL_KEY_HEADERS         = "header";
    private static final String MODEL_KEY_USER            = "user";
    private static final String MODEL_KEY_AUTH            = "auth";
    private static final String MODEL_KEY_HTTP_REQUEST    = "req";
    private static final String MODEL_KEY_HTTP_RESPONSE   = "res";

    static Map<?, ?> buildRequestModel(RequestContext rc) {
        HttpServletRequest request = rc.getReq();
        HttpServletResponse response = rc.getRes();

        HashMap<String, Object> requestMap = new HashMap<String, Object>();

        requestMap.put(MODEL_KEY_REQUEST_CONTEXT, rc);

        /*--------- Include user and auth ---------*/
        /**/
        Auth<?> auth = rc.getAuth();
        if (auth != null) {
            requestMap.put(MODEL_KEY_AUTH, auth);
            requestMap.put(MODEL_KEY_USER, auth.getUser());
        }

        /*--------- /Include user and auth ---------*/

        /* --------- Include the HTTPRequest and HTTPResponse/ --------- */
        requestMap.put(MODEL_KEY_HTTP_REQUEST, request);
        requestMap.put(MODEL_KEY_HTTP_RESPONSE, response);

        // add the context path
        requestMap.put(MODEL_KEY_CONTEXT_PATH, request.getContextPath());

        // add the pathInfo
        String pathInfo = rc.getPathInfo();
        requestMap.put(MODEL_KEY_PATH_INFO, pathInfo);

        //add the fullPath
        StringBuilder fullPathSB = new StringBuilder(request.getContextPath());
        fullPathSB.append(pathInfo);
        requestMap.put(MODEL_KEY_FULL_PATH, fullPathSB.toString());

        /* --------- Include the Request Params --------- */
        requestMap.put(MODEL_KEY_PARAMS, rc.getParamMap());
        /* --------- /Include the Request Params --------- */

        String queryString = request.getQueryString();
        requestMap.put(MODEL_KEY_QUERY_STRING, queryString);

        if (queryString != null && queryString.length() > 0) {
            requestMap.put(MODEL_KEY_HREF, fullPathSB.append('?').append(queryString));
        } else {
            requestMap.put(MODEL_KEY_HREF, fullPathSB.toString());
        }

        /* --------- Include Headers --------- */
        HashMap<String, Object> headersMap = new HashMap<String, Object>();
        requestMap.put(MODEL_KEY_HEADERS, headersMap);

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headersMap.put(headerName, headerValue);
        }
        /* --------- /Include Headers --------- */

        /* --------- Include Cookies --------- */
        requestMap.put(MODEL_KEY_COOKIES, rc.getCookieMap());
        /* --------- /Include Cookies --------- */

        return requestMap;
    }

}


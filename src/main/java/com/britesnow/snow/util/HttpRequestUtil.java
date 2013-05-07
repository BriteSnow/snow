/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpRequestUtil {

    /* --------- Cookie Methods --------- */

    static public String getCookieStringValue(HttpServletRequest req, String name) {
        assert (req != null) : "Cannot accept a null 'request' argument";
        assert (name != null) : "Cannot accept a null 'name' argument";

        // Loop through the cookies, and return the value for the matching
        // cookie
        Cookie cookie = getCookie(req, name);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }

    /**
     * @param res
     * @param name
     * @param value if null, does nothing
     * @param persistent
     */
    static public void setCookieValue(HttpServletResponse res, String name, Object value, boolean persistent) {
        if (value != null) {
            Cookie cookie = new Cookie(name, value.toString());
            cookie.setPath("/");
            cookie.setMaxAge(3600 * 24 * 364); // set expiration to one year.
            res.addCookie(cookie);
        }
    }

    static public void removeCookie(HttpServletRequest req, HttpServletResponse res, String name) {
        Cookie cookie = getCookie(req, name);
        if (cookie != null) {
            cookie.setPath("/");
            cookie.setMaxAge(0);
            res.addCookie(cookie);
        }
    }

    static public Cookie getCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String cookieName = cookie.getName();
                if (name.equals(cookieName)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /* --------- /Cookie Methods --------- */

    /* --------- Get Param Values --------- */
    @SuppressWarnings("unchecked")
    public static <T extends Enum> T getParamAsEnum(HttpServletRequest request, String name, Class<T> enumType) {
        String valueStr = request.getParameter(name);
        if (valueStr == null || valueStr.length() == 0) {
            return null;
        } else {
            return (T) Enum.valueOf(enumType, valueStr);
        }
    }

    public static <T> T getParam(HttpServletRequest request, String name, Class<T> cls, T defaultValue) {
        String valueStr = request.getParameter(name);
        return ObjectUtil.getValue(valueStr, cls, defaultValue);
    }

    /* --------- Get Param Values --------- */
    
    /*--------- Query String ---------*/
    /**
     * Simple methods that return a map name/value pair given a queryString like "name=toto&last=foo". <br />
     * Note: today does not support multivalue
     * @param queryString should not contain the leading '?'. Just name/value seperated by '&'.
     * @return Name/Value Map. Empty map if queryString is null or does not contain any name/value.
     */
    public static Map<String,Object> getMapFromQueryString(String queryString){
        Map<String,Object> map = new HashMap<String, Object>();
        if (queryString != null){
            for (String string : queryString.split("&")){
                String[] nameAndValue = string.split("=");
                if (nameAndValue != null && nameAndValue.length > 1){
                    map.put(urlDecode(nameAndValue[0]), urlDecode(nameAndValue[1]));
                }
            }
        }
        return map;
    }
    
    
    public static String getQueryStringFromMap(Map map){
        StringBuilder sb = new StringBuilder();
        boolean firstParam = true;
        if (map != null){
            for (Object key : map.keySet()){
                if (!firstParam){
                    sb.append('&');
                }else{
                    firstParam = false;
                }
                sb.append(urlEncode(key.toString()));
                
                Object value = map.get(key);
                if (value != null){
                    sb.append('=').append(urlEncode(value.toString()));
                }
            }
        }
        
        return sb.toString();
    }
    /*--------- /Query String ---------*/


    /**
     * same as {@link URLEncoder#encode(String, String)} except forces UTF-8 w/ no checked exception.
     * @throws IllegalStateException if the platform doesn't support UTF-8...shouldn't happen in practice.
     */
    public static String urlEncode(String str) {
      try {
            return str == null ? null : URLEncoder.encode(str, "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            throw new IllegalStateException("Your system must support UTF-8");
        }
    }

    /**
     * same as {@link URLDecoder#decode(String, String)} except forces UTF-8 w/ no checked exception.
     * @throws IllegalStateException if the platform doesn't support UTF-8...shouldn't happen in practice.
     */
    public static String urlDecode(String str) {
        try {
            return str == null ? null : URLDecoder.decode(str, "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            throw new IllegalStateException("Your system must support UTF-8");
        }
    }
}

package com.britesnow.snow.web;

import javax.servlet.http.HttpServletResponse;

public class AbortWithHttpRedirectException extends RuntimeException {

    private static final long serialVersionUID = 4403962118229585702L;
    
    private String location;
    private int redirectCode;

    public AbortWithHttpRedirectException(String location) {
        this(location, HttpServletResponse.SC_MOVED_TEMPORARILY);
    }

    public AbortWithHttpRedirectException(String location, int redirectCode) {
        super(location + " [" + redirectCode + "]");

        this.location = location;
        this.redirectCode = redirectCode;
    }

    public String getLocation() {
        return location;
    }

    public int getRedirectCode() {
        return redirectCode;
    }
}

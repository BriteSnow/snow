package com.britesnow.snow.web.path;

import com.britesnow.snow.web.RequestContext;

/**
 * Resolve a path to a resource path for a particular RequestContext. 
 * 
 * Override the Guice binding for this if you want to have conditional resourcePath resolution. 
 * For example, for a multisite, the resourcePathResolver could have different resourcePath for the same path
 * if they have different host. 
 * 
 */
public interface ResourcePathResolver {

    
    public String resolve(RequestContext rc, String path);
}

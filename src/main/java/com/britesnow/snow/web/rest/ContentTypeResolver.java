package com.britesnow.snow.web.rest;

import com.britesnow.snow.web.RequestContext;

/**
 * Resolve the content type for a request for the Web REST bindings (@WebGet, @WebPost) type of 
 * methods.  
 * 
 * By default, the DefaultTypeResolver is bound, but this can be overridden at the guice application modules. 
 * 
 * @author jeremychone
 * @since  2.0.0
 * 
 */
public interface ContentTypeResolver {
    
    public String resolveContentType(RequestContext rc);

}

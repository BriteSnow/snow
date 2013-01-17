package com.britesnow.snow.web.rest;

import javax.inject.Singleton;

import com.britesnow.snow.web.RequestContext;

/**
 * Default ContentTypeResolver. For now, just return "application/json" content type.  
 * 
 * @author jeremychone
 * @since 2.0.0
 */
@Singleton
public class DefaultContentTypeResolver implements ContentTypeResolver {

    @Override
    public String resolveContentType(RequestContext rc) {
        return "application/json";
    }

}

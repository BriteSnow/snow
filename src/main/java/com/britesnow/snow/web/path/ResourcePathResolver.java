package com.britesnow.snow.web.path;

import com.britesnow.snow.web.RequestContext;

public interface ResourcePathResolver {

    
    public String resolve(RequestContext rc);
}

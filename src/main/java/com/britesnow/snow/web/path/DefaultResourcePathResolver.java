package com.britesnow.snow.web.path;

import com.britesnow.snow.web.RequestContext;

public class DefaultResourcePathResolver implements ResourcePathResolver {

    @Override
    public String resolve(RequestContext rc) {
        return rc.getPathInfo();
    }

}

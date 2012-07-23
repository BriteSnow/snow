package com.britesnow.snow.web.path;

import com.britesnow.snow.web.RequestContext;

/**
 * Default resolver from PathInfo to ResourcePath.
 * 
 * For now, it just return the same path, but in later version, it will remove the eventual timestamp (i.e. __***.***)
 * at the of a pathInfo (before the extension)
 * 
 * @author jeremychone
 */
public class DefaultResourcePathResolver implements ResourcePathResolver {

    @Override
    public String resolve(String pathInfo, RequestContext rc) {
        // by default, just return the path.
        return pathInfo;
    }

}

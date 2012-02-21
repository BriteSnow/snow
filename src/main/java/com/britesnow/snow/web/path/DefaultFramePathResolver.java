package com.britesnow.snow.web.path;

import com.britesnow.snow.web.RequestContext;



public class DefaultFramePathResolver implements FramePathResolver{
    static final private String FRAME_DEFAULT_HTML            = "/frame-default";
    static final private String FRAME_DEFAULT_EMPTY           = "/frame-empty";
    
    @Override
    public String resolve(RequestContext rc) {
        
        String pathInfo = rc.getPathInfo();
        
        int lastSlashIdx = pathInfo.lastIndexOf("/");
        String pageName = (lastSlashIdx != -1) ? pathInfo.substring(lastSlashIdx + 1) : pathInfo;
        if (pageName.startsWith("_")) {
            return FRAME_DEFAULT_EMPTY;
        } else {
            return FRAME_DEFAULT_HTML;
        }
        
    }
    
}
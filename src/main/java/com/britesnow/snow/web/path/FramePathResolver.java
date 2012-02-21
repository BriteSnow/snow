package com.britesnow.snow.web.path;

import com.britesnow.snow.web.RequestContext;


public interface FramePathResolver{
    
    public String resolve(RequestContext rc);
}
package com.britesnow.snow.web.path;

import com.britesnow.snow.web.RequestContext;


public interface FramePathsResolver{
    
    public String[] resolve(RequestContext rc);
}
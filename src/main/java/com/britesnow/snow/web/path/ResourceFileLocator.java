package com.britesnow.snow.web.path;

import java.io.File;

import com.britesnow.snow.web.RequestContext;

/**
 * Locate a resource File from a resourcePath to a local file on the file system. Note that the resourcePath should have been 
 * resolved by a ResourcePathResolver first.  
 * 
 * @author jeremychone
 *
 */
public interface ResourceFileLocator {

    
    public File locate(String resourcePath, RequestContext rc);
    
}

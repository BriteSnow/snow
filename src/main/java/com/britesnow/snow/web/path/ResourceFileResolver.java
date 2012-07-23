package com.britesnow.snow.web.path;

import java.io.File;

import com.britesnow.snow.web.RequestContext;

/**
 * Resolve File from a resourcePath to a local file on the file system. Note that the resourcePath should have been 
 * resolved by a ResourcePathResolver first.  
 * 
 * @author jeremychone
 *
 */
public interface ResourceFileResolver {

    
    public File resolve(String resourcePath, RequestContext rc);
    
}

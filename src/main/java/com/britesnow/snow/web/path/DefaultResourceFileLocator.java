package com.britesnow.snow.web.path;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.binding.WebAppFolder;

/**
 * Default ResourceFile locator. Just assume that the resource path is relative to the webAppFolder. 
 * 
 * @author jeremychone
 */
@Singleton
public class DefaultResourceFileLocator implements ResourceFileLocator {

    @Inject 
    private @WebAppFolder File  webAppFolder;
    
    public File locate(String resourcePath, RequestContext rc){
        return new File(webAppFolder,resourcePath);
    }
}

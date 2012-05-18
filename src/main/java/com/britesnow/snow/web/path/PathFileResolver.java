package com.britesnow.snow.web.path;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.britesnow.snow.web.binding.WebAppFolder;

/**
 * Resolve File from a path (usually a resourcePath). Note that the resourcePath might be resolved should have been 
 * resolved by a ResourcePathResolver first.  
 * 
 * @author jeremychone
 *
 */
@Singleton
public class PathFileResolver {

    @Inject 
    private @WebAppFolder File  webAppFolder;
    
    public File resolve(String path){
        return new File(webAppFolder,path);
    }
}

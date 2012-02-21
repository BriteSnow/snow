package com.britesnow.snow.web.path;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.britesnow.snow.web.binding.WebAppFolder;

@Singleton
public class PathFileResolver {

    @Inject 
    private @WebAppFolder File  webAppFolder;
    
    public File resolve(String path){
        return new File(webAppFolder,path);
    }
}

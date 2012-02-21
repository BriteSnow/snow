package com.britesnow.snow.testsupport.mock;

import java.io.File;

import javax.servlet.ServletContext;


import com.britesnow.snow.web.Application;
import com.britesnow.snow.web.ApplicationLoader;
import com.google.inject.Injector;

public class ApplicationLoaderMock extends ApplicationLoader {

    public ApplicationLoaderMock(File webAppFolder, ServletContext servletContext) {
        super(webAppFolder, servletContext);
    }
    
    // for testing only
    public Application getApplication(){
        return appInjector.getInstance(Application.class);
    }
    
    // for testing only
    public Injector getApplicationInjector(){
        return appInjector;
    }
    
    public ApplicationLoaderMock load() throws Exception{
        super.load();
        return this;
    }

}

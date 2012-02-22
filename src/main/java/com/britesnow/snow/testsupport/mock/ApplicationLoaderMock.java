package com.britesnow.snow.testsupport.mock;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.britesnow.snow.web.Application;
import com.britesnow.snow.web.ApplicationLoader;
import com.google.inject.Injector;
import com.google.inject.Module;

public class ApplicationLoaderMock extends ApplicationLoader {

    public ApplicationLoaderMock(File webAppFolder) {
        super(webAppFolder, null);
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
    
    public ApplicationLoaderMock load(List<Module> applicationModules,Map appProperties){
        super.load(applicationModules, appProperties);
        return this;
    }

}

/*
 * Copyright 2009-2012 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
*/
package com.britesnow.snow.web;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.ServletContext;


import com.britesnow.snow.web.binding.CurrentRequestContext;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * Provide the following: 
 * 
 * - ServletContext
 * - CurrentRequestContextHolder
 * - CurrentRequestContext
 * 
 * @author jeremychone
 *
 */
public class RootWebModule extends AbstractModule {

    private ServletContext servletContext = null;

    
    RootWebModule(@Nullable ServletContext servletContext){
        this.servletContext = servletContext; 

    }
    
    
    @Override
    protected void configure() {
        if (servletContext != null){
            bind(ServletContext.class).toInstance(servletContext);
        }
    }
    
    
    
    @CurrentRequestContext
    @Provides
    @Inject
    public RequestContext providesCurrentRequestContext(@Nullable CurrentRequestContextHolder rcHolder) {
        if (rcHolder != null) {
            return rcHolder.getCurrentRequestContext();
        }
        return null;
    }

    @Provides
    @Inject
    public CurrentRequestContextHolder providesCurrentRequestContextHolder(WebController webController) {
        return webController.getCurrentRequestContextHolder();
    }    
    
    

}

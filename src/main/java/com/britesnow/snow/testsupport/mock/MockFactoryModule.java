package com.britesnow.snow.testsupport.mock;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.AbstractModule;

public class MockFactoryModule extends AbstractModule {

    @Override
    protected void configure() {
        
        bind(ServletOutputStream.class).to(ServletOutputStreamMock.class);
        bind(HttpServletRequest.class).to(HttpServletRequestMock.class);
        bind(HttpServletResponse.class).to(HttpServletResponseMock.class);
        bind(ServletContext.class).to(ServletContextMock.class);
    }

    
}

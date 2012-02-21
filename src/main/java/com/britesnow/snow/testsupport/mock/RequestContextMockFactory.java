package com.britesnow.snow.testsupport.mock;


import com.google.inject.Guice;
import com.google.inject.Injector;

public class RequestContextMockFactory {

    public enum RequestMethod{
        POST, GET
    }
    private Injector injector;
    
    public RequestContextMockFactory init(){
        injector = Guice.createInjector(new MockFactoryModule());
        return this;
    }
    
    public RequestContextMock createRequestContext(RequestMethod method,String pathInfo){
        assertInit();
        
        RequestContextMock rc = injector.getInstance(RequestContextMock.class);
        rc.setRequestMethod(method.name());
        rc.setPathInfo(pathInfo);
        rc.init();
        
        return rc;
    }
    
    
    private void assertInit(){
        if (injector == null){
            throw new RuntimeException("MockFactory not initialized, needs to call init() first");
        }
    }
}

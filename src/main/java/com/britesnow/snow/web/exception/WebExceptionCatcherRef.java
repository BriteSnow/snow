package com.britesnow.snow.web.exception;

import java.lang.reflect.Method;

import com.britesnow.snow.web.exception.annotation.WebExceptionCatcher;

public class WebExceptionCatcherRef {
    @SuppressWarnings("unused")
    private WebExceptionCatcher        webExceptionHandler;

    private Class<? extends Throwable> throwableClass;

    private Class                      webClass;
    private Method                     method;

    public WebExceptionCatcherRef(Class webClass, Method method, WebExceptionCatcher webExceptionHandler) {

        this.webClass = webClass;
        this.method = method;
        
        this.webExceptionHandler = webExceptionHandler;

        Class[] paramTypes = method.getParameterTypes();
        if (paramTypes.length < 1 || paramTypes[0].getClass().isAssignableFrom(Throwable.class)) {
            throw new RuntimeException("Snow WebApp Initialization failed: WebExceptionHandler " + method.getName()
                                    + " must have a Throwable as first parameter");
        }
        throwableClass = paramTypes[0];
    }

    public Class getThrowableClass() {
        return throwableClass;
    }

    public Class getWebClass(){
        return webClass;
    }
    
    public Method getWebMethod(){
        return method;
    }
}

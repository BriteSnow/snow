package com.britesnow.snow.web.exception;

import java.lang.reflect.Method;

import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.exception.annotation.WebExceptionCatcher;
import com.google.common.base.Throwables;

public class WebExceptionCatcherRef {
    @SuppressWarnings("unused")
    private WebExceptionCatcher        webExceptionHandler;

    private Class<? extends Throwable> throwableClass;

    private Object                     webObject;
    private Method                     method;

    public WebExceptionCatcherRef(Object webObject, Method method, WebExceptionCatcher webExceptionHandler) {

        this.webObject = webObject;
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

    
    public void invoke(Throwable e, WebExceptionContext webExceptionException, RequestContext rc) {
        Object[] values = new Object[] { e, webExceptionException, rc };
        try {
            method.invoke(webObject, values);
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }
}

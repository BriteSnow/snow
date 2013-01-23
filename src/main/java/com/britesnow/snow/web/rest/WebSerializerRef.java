package com.britesnow.snow.web.rest;

import java.lang.reflect.Method;

import com.britesnow.snow.web.handler.WebHandlerRef;

public class WebSerializerRef extends WebHandlerRef{

    protected WebSerializerRef(Class webClass, Method method) {
        super(webClass, method, null);
    }

}

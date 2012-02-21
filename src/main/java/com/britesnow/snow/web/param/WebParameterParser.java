package com.britesnow.snow.web.param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.britesnow.snow.web.RequestContext;

public interface WebParameterParser<A extends Annotation> {

    public Class<A> getAnnotationClass();

    public <T> T getParameterValue(Method m, A annotation, Class<T> paramType, RequestContext rc);
}

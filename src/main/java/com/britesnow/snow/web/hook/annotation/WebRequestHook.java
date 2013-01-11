package com.britesnow.snow.web.hook.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.britesnow.snow.web.hook.ReqStep;

@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.METHOD)
public @interface WebRequestHook {

    ReqStep step();
}

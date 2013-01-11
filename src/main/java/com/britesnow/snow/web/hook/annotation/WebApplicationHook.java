package com.britesnow.snow.web.hook.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.britesnow.snow.web.hook.AppStep;

@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.METHOD)
public @interface WebApplicationHook {
    AppStep step();
}

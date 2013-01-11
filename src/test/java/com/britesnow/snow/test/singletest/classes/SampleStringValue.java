package com.britesnow.snow.test.singletest.classes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME) 
@Target({ FIELD, PARAMETER, METHOD })
public @interface SampleStringValue {

}

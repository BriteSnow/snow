/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.binding;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;
import  java.lang.annotation.ElementType;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@BindingAnnotation 
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD }) 
@Retention(RUNTIME)
public @interface LeafPaths {

}

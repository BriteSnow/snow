/* Copyright 2010 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.param.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Add this annotation to a @WebAction or @WebModel method inject. 
 * Usage: 
 * <pre>
 * \@WebAction
 * public void addUser(@WebMode; Map m){...}
 * </pre>
 * 
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.PARAMETER)
public @interface WebModel {

}

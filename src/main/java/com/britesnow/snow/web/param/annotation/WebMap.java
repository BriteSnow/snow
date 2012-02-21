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
 * Add this annotation to a @WebAction or @WebModel method inject. <br />
 * Note that this notation is optional in @WebModel, since the first argument must be a Map.
 * Usage: 
 * <pre>
 * \@WebAction
 * public void addUser(@WebMap Map m){...}
 * </pre>
 * 
 * @author Jeremy Chone
 * @date Apr 12, 2010
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.PARAMETER)
public @interface WebMap {

}

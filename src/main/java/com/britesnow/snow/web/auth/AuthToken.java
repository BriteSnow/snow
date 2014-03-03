/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.auth;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>The authentication token returned by the AuthService. It provides basic role model with the Type, but typical use just use the User
 * while role/privilege model is application specific.</p>
 * @param <T>
 */
public class AuthToken<T> {

	private T user = null;

    public AuthToken() {
    }

	public AuthToken(T user) {
		this.user = user;
	}

    /* --------- Getters & Setters --------- */
    public T getUser() {
        return user;
    }

    public AuthToken<T> setUser(T user) {
        this.user = user;
		return this;
    }

    /* --------- /Getters & Setters --------- */

}

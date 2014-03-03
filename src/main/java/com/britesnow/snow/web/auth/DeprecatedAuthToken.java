package com.britesnow.snow.web.auth;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>This is just in case some developers still use the old version of the AuthToken which had a very
 * basic role/privilege model. At this point, we think that a role/privilege is better handled by the application layer rather
 * than trying to than polluting the AuthToken pattern for it. </p>
 *
 * <p>So, if your code used the old AuthToken and relied on the with the methods below, then you can swith to use this class,
 * otherwise, try to NOT use this class, it will eventually go away.</p>
 *
 * Created by jeremychone on 3/3/14.
 */
@Deprecated
public class DeprecatedAuthToken<T> extends AuthToken<T> {
	public enum Type {
		root, admin, user, visitor;
	}
	private Type                type       = Type.visitor;
	private Map<String, String> groupNames = new HashMap<String, String>();

	public DeprecatedAuthToken(Type type) {
		setType(type);
	}

	public boolean belongTo(String groupName) {
		return groupNames.containsKey(groupName);
	}


	/*--------- Getters ---------*/
	public boolean getHasRootRights() {
		return (type == Type.root) ? true : false;
	}

	public boolean getHasAdminRights() {
		switch (type) {
			case root:
			case admin:
				return true;
			default:
				return false;
		}
	}

	public boolean getHasUserRights() {
		switch (type) {
			case root:
			case admin:
			case user:
				return true;
			default:
				return false;
		}
	}
	public boolean getHasVisitorRights() {
		return true; //for now, everybody is at least visitor right
	}
    /*--------- /Getters ---------*/

	public Type getType() {
		return type;
	}

	public AuthToken<T> setType(Type type) {
		this.type = type;
		if (type != null) {
			groupNames.put(type.name(), type.name());

			//add the "admin" group if it is a root Auth type
			switch (type) {
				case root:
				case admin:
					groupNames.put("admin", "admin");
					break;
				default:
					break;

			}
		}
		return this;
	}
}

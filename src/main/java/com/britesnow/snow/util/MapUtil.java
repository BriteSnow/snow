/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.util;

import java.util.*;

public class MapUtil {


	// --------- Nested Map --------- //

	
	
	/**
	 * @param m the nested map
	 * @param namePath the namePath (i.e. "product.name")
	 */
	public static String  getDeepValue(Map m, String namePath) {
		return getDeepValue(m,namePath,String.class);
	}
	
	/**
	 * Convenient methods for the getNestedValue(Map m, String namePath, Class cls, T defaultValue) with defaultValue == null
	 * @param <T>
	 * @param m
	 * @param namePath
	 * @param cls
	 * @return
	 */
	public static <T> T getDeepValue(Map m, String namePath, Class<T> cls) {
		return getDeepValue(m,namePath,cls,null);
	}
	
	/**
	 * Return the value given a namePath (i.e. "product.name") from a nested map. Return defaultValue if map, namePath, or value do not exist.
	 * 
	 * @param <T>
	 * @param m the Map
	 * @param namePath the namePath (i.e. "product.name")
	 * @param cls the class to convert the value to
	 * @param defaultValue the default value in case the map, namePath, or value do not exist
	 * @return
	 */
	public static <T> T getDeepValue(Map m, String namePath, Class<T> cls, T defaultValue) {
        if (m != null && namePath != null) {
            String[] names = namePath.split("\\.");
            Map tmpMap = m;
            for (int i = 0; i < names.length - 1; i++) {
                Object v = tmpMap.get(names[i]);
                if (v != null && v instanceof Map) {
                    tmpMap = (Map) v;
                } else {
                    return defaultValue;
                }
            }

            //if we are here, then, we got the final name
            Object v = tmpMap.get(names[names.length - 1]);
            if (v != null) {
                if (v.getClass() == cls) {
                    return (T) v;
                } else {
                    return ObjectUtil.getValue(v.toString(), cls, defaultValue);
                }
            } else {
                return defaultValue;
            }

        }

        return defaultValue;		
	}
	// --------- /Nested Map --------- //

    /**
     * Return a nestedMap of Object (HashMap) from an Array of objects (the '.'
     * delimits sub map). first/odds elements are the key, even are the values.
     * If the array has an odd number of elements, then the last key/value will
     * have an null value;
     * 
     * @param objs
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> deepMapIt(Object... objs) {
        HashMap<String, Object> m = new HashMap<String, Object>();

        for (int i = 0; i < objs.length; i += 2) {
            Object key = objs[i];
            if (i + 1 < objs.length) {

                Object value = objs[i + 1];

                //if the key is a string, then, add the extra logic to support nested map with the "." notation
                if (key instanceof String) {
                    String name = (String) objs[i];
                    //If there is no '.', then, just add the value. 
                    if (name.indexOf('.') == -1) {
                        m.put(name, value);
                    }
                    //
                    else {
                        Map subMap = m;

                        String[] names = name.split("\\.");
                        for (int j = 0; j < names.length; j++) {
                            String subName = names[j];
                            //if it is the last, then, just set the value
                            if ((j + 1) == names.length) {
                                subMap.put(subName, value);
                            } else {
                                Map tmpMap = (Map) subMap.get(subName);
                                if (tmpMap == null) {
                                    tmpMap = new HashMap();
                                    subMap.put(subName, tmpMap);
                                }
                                subMap = tmpMap;
                            }

                        }
                    }
                }
                //otherwise, if the key is not a string, but we convert to a string.
                else {
                    m.put(key.toString(), value);
                }

            }
            //if it is the last name/value pair and it does not have any value
            else {
                m.put(key.toString(), null);
            }
        }

        return m;
    }




    /**
     * Return a Map of Object (HashMap) from an Array of objects. first/odds
     * elements are the key, even are the values. If the array has an odd number
     * of elements, then the last key/value will have an null value;
     * 
     * @param objs
     * @return
     */

    public static Map<?, ?> mapIt(Object... objs) {
        HashMap<Object, Object> m = new HashMap<Object, Object>();

        for (int i = 0; i < objs.length; i += 2) {
            Object key = objs[i];
            if (i + 1 < objs.length) {
                Object value = objs[i + 1];
                m.put(key, value);

            } else {
                m.put(key, null);
            }
        }

        return m;
    }

    /**
     * 
     * @param map
     *            Map to search.
     * @param string
     *            String to match
     * @return Return true if at least one key start with the value "string".
     *         Return false if nothing match or if map or string are null.
     */
    public static boolean hasKeyStartsWith(Map map, String string) {
        if (map != null && string != null) {
            for (Object key : map.keySet()) {
                String keyStr = key.toString();
                if (keyStr.startsWith(string)) {
                    return true;
                }
            }
        }
        return false;
    }

    // --------- Set Methods --------- //
    /**
     * Build a set from the objs. If objs is null, then return an empty Set.
     * 
     * @param objs
     *            the Object array to create the set with
     * @return a Set of objs (an empty Set if objs is null)
     */
    @SuppressWarnings("unchecked")
    public static Set setIt(Object... objs) {
        Set set = new HashSet();
        if (objs != null) {
            for (Object obj : objs) {
                set.add(obj);
            }
        }

        return set;

    }    
    // --------- /Set Methods --------- //
}

/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class ObjectUtil {
    static private Logger logger = LoggerFactory.getLogger(ObjectUtil.class);
    
    static NumberFormat numberFormat = NumberFormat.getInstance(Locale.US); 
    
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    
    //NOTE: the beanUtil use the ObjectUtil.getValue to convert
    static BeanUtilsBean beanUtilsBean = new BeanUtilsBean(new ConvertUtilsBean() {

                                           @SuppressWarnings("unchecked")
                                           @Override
                                           public Object convert(String value, Class clazz) {

                                               if (clazz.isEnum()) {
                                                   if (value != null && value.length() > 0){
                                                       return Enum.valueOf(clazz, value);
                                                   }else{
                                                       return null;
                                                   }
                                                   
                                               } else {
                                                   return getValue(value, clazz,null);
                                               }

                                           }

                                       }, new PropertyUtilsBean() {

                                           @Override
                                           public Object getProperty(Object bean, String name) throws IllegalAccessException,
                                                                   InvocationTargetException, NoSuchMethodException {

                                               Object property = super.getProperty(bean, name);

                                               //if null, then, try the best to create the target class
                                               if (property == null) {
                                                   Class propClass = getPropertyType(bean, name);
                                                   //if it is 
                                                   if (propClass != null && !propClass.isInterface()) {
                                                       try {
                                                           property = propClass.newInstance();
                                                           setProperty(bean, name, property);
                                                       } catch (InstantiationException e) {
                                                           throw new InvocationTargetException(e, "Cannot instantiate class "
                                                                                   + propClass + " for property " + name);
                                                       }
                                                   }
                                               }
                                               return property;

                                           }

                                       });

    @Deprecated
    public static void copyNotNull(Object src, Object dest) {
        try {

            PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(src);
            for (PropertyDescriptor pd : pds) {
                String name = pd.getName();
                if (pd.getWriteMethod() != null) {
                    Object value = PropertyUtils.getProperty(src, name);
                    if (value != null) {
                        PropertyUtils.setProperty(dest, name, value);
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw (new RuntimeException(e));
        }
    }

    /**
     * Copy all the properties are that not null, or not empty (for List and
     * array objects) to the dest object.
     * 
     * @param src
     * @param dest
     */
    @Deprecated
    public static void copyNotNullNotEmpty(Object src, Object dest) {
        try {

            PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(src);
            for (PropertyDescriptor pd : pds) {
                String name = pd.getName();

                if (pd.getWriteMethod() != null) {
                    Object value = PropertyUtils.getProperty(src, name);
                    boolean copy = true;

                    if (value == null) {
                        copy = false;
                    } else if (value instanceof List && ((List) value).size() < 1) {
                        copy = false;
                    } else if (value.getClass().isArray() && ((Object[]) value).length < 1) {
                        copy = false;
                    }

                    if (copy) {
                        PropertyUtils.setProperty(dest, name, value);
                    }
                }
            }
            // BeanUtils.copyProperties(dest, src);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw (new RuntimeException(e));
        }
    }

    /**
     * Safe equal methods for two object. If both are null, return true,
     * otherwise, if only one is null, return false, otherwise, if they match
     * return true if not false.
     * 
     * @param obj1
     * @param obj2
     * @return
     */
    public static boolean equal(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        //if we are hear, one of the obj must be not null, so, if one null, return false
        if (obj1 == null || obj2 == null) {
            return false;
        }

        return obj1.equals(obj2);
    }

    public static <T> T populate(T bean, Map map) {
        try {
            if (bean != null && map != null) {
                beanUtilsBean.populate(bean, map);
            }
            return bean;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <div class="notes"> <strong>Notes:</strong>
     * <ul>
     * <li>So far does not safeguard any type conversion (will throw an runtime
     * exception if parsing fail)</li>
     * </ul>
     * </div>
     * 
     * @param <T>
     * @param values
     *            String arrays of the value to be converted
     * @param cls
     * @param defaultValues
     * @return The typed array given a value of the String.
     */
    public static final <T> T getValue(String[] values, Class<T> cls, T defaultValues) {
        if (values != null && cls.isArray()) {
            Class compCls = cls.getComponentType();
            T resultArray = (T) Array.newInstance(compCls, values.length);
            int i = 0;
            for (String v : values) {
                Object r = getValue(v, compCls, null);
                Array.set(resultArray, i++, r);
            }
            return resultArray;
        } else {
            return defaultValues;
        }

    }

    public static final <T> T getValue(String valueStr, Class<T> cls, T defaultValue) {
        if (valueStr == null) {
            return defaultValue;
        } else {
            try {
                if (cls == String.class) {
                    return (T) valueStr;
                } else if (valueStr.length() > 0) {

                    if (cls.isArray()) {
                        return getValue(new String[] { valueStr }, cls, defaultValue);
                    } else if (cls == Integer.class) {
                        Integer value = numberFormat.parse(valueStr).intValue();
                        return (T) value;
                    } else if (cls == Long.class) {
                        Long value = numberFormat.parse(valueStr).longValue();
                        return (T) value;
                    } else if (cls == Float.class) {
                        Float value = numberFormat.parse(valueStr).floatValue();
                        return (T) value;
                    } else if (cls == Double.class) {
                        Double value = numberFormat.parse(valueStr).doubleValue();
                        return (T) value;
                    } else if (cls == Boolean.class) {
                        if ("true".equals(valueStr)) {
                            return (T) new Boolean(true);
                        } else {
                            return (T) new Boolean(false);
                        }
                    } else if (cls.isEnum()) {
                        try {
                            return (T) Enum.valueOf((Class<Enum>) cls, valueStr);
                        } catch (IllegalArgumentException e) {
                            return defaultValue;
                        }
                    }else if(cls==Date.class){
                        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
                        return (T) new java.util.Date(sdf.parse(valueStr).getTime());
                    }
                } else {
                    return defaultValue;
                }
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private static Set<Class> primitiveClasses = MapUtil.setIt(String.class, Integer.class, Long.class, Float.class,
                                                                       Boolean.class);

    public static final boolean isPrimitive(Class cls) {
        return primitiveClasses.contains(cls);
    }
}

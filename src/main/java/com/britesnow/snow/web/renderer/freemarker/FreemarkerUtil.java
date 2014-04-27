/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.renderer.freemarker;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;

public class FreemarkerUtil {
    static private Logger logger = LoggerFactory.getLogger(FreemarkerUtil.class);
    
    static public <C> C getDataModel(String mapName, Class<C> c) {
        Environment env = Environment.getCurrentEnvironment();

        TemplateHashModel rootModel = env.getDataModel();
        Object tmpObj = rootModel;
        try {
            for (String key : mapName.split("\\.")) {
                if (tmpObj instanceof TemplateHashModel) {
                    tmpObj = ((TemplateHashModel) tmpObj).get(key);
                } 
            }

        } catch (Exception e) {
            logger.warn(e.getMessage());
            tmpObj = null;
        }

        return getParamAs(tmpObj, c);
    }

    /**
     * Convenient methods to return a Freemarker param wrapped object. 
     * NOTE: It assumes that the <code>Class&lt;C&gt; c</code> will
     * be the correct type.
     * 
     * @param <C>
     *            Class to return
     * @param params
     *            Freemarker params
     * @param name
     *            param name
     * @param c
     *            Object class type to return
     * @return
     */

    static public <C> C getParam(Map params, String name, Class<C> c) {

        Object value = params.get(name);

        return getParamAs(value, c);
    }

    @SuppressWarnings("unchecked")
    static public <C> C getParamAs(Object value, Class<C> c) {
        try {
            if (value instanceof SimpleScalar) {
                return (C) value.toString();
            } else if (value instanceof BeanModel) {
                return (C) ((BeanModel) value).getWrappedObject();
            } else if (value instanceof TemplateBooleanModel) {
                return (C) new Boolean(((TemplateBooleanModel) value).getAsBoolean());
            } else if (value instanceof SimpleNumber){
                if (c == Integer.class){
                    return (C) new Integer(((SimpleNumber)value).getAsNumber().intValue());
                }else if (c == Long.class){
                    return (C) new Long(((SimpleNumber)value).getAsNumber().intValue());
                }
            }
        } catch (TemplateModelException e) {
            // TODO Auto-generated catch block
            logger.error(e.getMessage());
        }
        return null;
    }
    
    
    static public Object getValue(Object freemarkerValue){
        Object value = null;
        try {
            if (freemarkerValue instanceof SimpleScalar) {
                value = freemarkerValue.toString();
            } else if (value instanceof BeanModel) {
                value =  ((BeanModel) value).getWrappedObject();
            } else if (value instanceof TemplateBooleanModel) {
                value = new Boolean(((TemplateBooleanModel) value).getAsBoolean());
            } else if (value instanceof SimpleNumber){
                value = new Long(((SimpleNumber)value).getAsNumber().intValue());
            }
        } catch (TemplateModelException e) {
            // TODO Auto-generated catch block
            logger.error(e.getMessage());
        }
        return value;        
    }

}

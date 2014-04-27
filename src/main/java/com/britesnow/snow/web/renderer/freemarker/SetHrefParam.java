/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.renderer.freemarker;

import java.util.List;
import java.util.Map;

import com.britesnow.snow.util.HttpRequestUtil;


import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;


/**
 * 
 * Set/Override some queryParam to an existing href. 
 * <pre>
 * arg0 href query (i.e., "/product" or "/product?prodId=234")
 * arg1 queryParams to override (i.e., "prodId=11" or "prodId=11&amp;itemId=123")
 * </pre>
 * Examples: 
 * <pre>
 * ..href='${setHrefParam(r.href,"showAllTasks=true")}'...
 * </pre>
 * NOTE: Does not support multivalue properties (the queryParams will override them)
 * 
 * @author Jeremy Chone
 */
public class SetHrefParam implements TemplateMethodModelEx {

    /** 
     * 
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    @Override
    public Object exec(List args) throws TemplateModelException {

        String href = args.get(0).toString();
        String overrideParams = args.get(1).toString();

        StringBuilder sb = new StringBuilder();

        int hrefQueryStartIdx = href.indexOf('?');

        //if the href does not have any '?', then, just add the params
        if (hrefQueryStartIdx == -1) {
            return sb.append(href).append('?').append(overrideParams).toString();
        } else {
            String hrefParam = href.substring(hrefQueryStartIdx + 1);
            Map<String, Object> hrefParamMap = HttpRequestUtil.getMapFromQueryString(hrefParam);
            Map<String, Object> overParamMap = HttpRequestUtil.getMapFromQueryString(overrideParams);

            //override the hrefParamMap with the overParamMap
            for (String key : overParamMap.keySet()) {
                hrefParamMap.put(key, overParamMap.get(key));
            }
            
            String queryString = HttpRequestUtil.getQueryStringFromMap(hrefParamMap);
            return sb.append(href.substring(0,hrefQueryStartIdx)).append('?').append(queryString).toString();

        }

    }

}

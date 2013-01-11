/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.handler;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.britesnow.snow.web.handler.annotation.WebModelHandler;


public class WebModelHandlerRef extends WebHandlerRef implements PathMatcher {

    WebModelHandler webModel;

    public WebModelHandlerRef(Class webClass, Method method,ParamDef[] paramDefs,
                              WebModelHandler webModel) {
        super(webClass, method,paramDefs);
        this.webModel = webModel;
    }
    
    /* (non-Javadoc)
     * @see org.snowfk.web.method.PathMatcher#matchesPath(java.lang.String)
     */
    @Override
    public boolean matchesPath(String path){
        if (webModel != null && webModel.matches().length > 0){
            /**/
            for (String regex : webModel.matches()){
                Pattern pat = Pattern.compile(regex);
                Matcher mat = pat.matcher(path);
                Boolean matches = mat.matches();
                if (matches){
                    return true;
                }
                
            }
            return false;
        }else{
            return false;
        }
    }
    
    
    public String toString(){
        return "WebModelRef: " + method.getName();
    }
}

package com.britesnow.snow.web.rest;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

import com.britesnow.snow.web.handler.ParamDef;
import com.britesnow.snow.web.handler.WebHandlerRef;

public class WebRestRef extends WebHandlerRef {

    private String path = null;
    private Pattern pathPattern = null;
    private Map<Integer,String> pathVarByIdx = null;
    
    WebRestRef(Class webClass, Method method, ParamDef[] paramDefs) {
        super(webClass, method, paramDefs);
    }
    
    WebRestRef(Class webClass, Method method, ParamDef[] paramDefs, String path, Pattern pathPattern, Map<Integer,String> pathVarByIdx) {
        super(webClass, method, paramDefs);
        this.path = path;
        this.pathPattern = pathPattern;
        this.pathVarByIdx = pathVarByIdx;
    }

    public String getPath() {
        return path;
    }

    public Pattern getPathPattern() {
        return pathPattern;
    }

    public Map<Integer, String> getPathVarByIdx() {
        return pathVarByIdx;
    }

}

package com.britesnow.snow.web.rest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import com.britesnow.snow.util.Pair;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.HttpMethod;
import com.britesnow.snow.web.handler.ParamDef;

/**
 * Note: the registerWeb*** must be called only in init time, as the Map are not concurrent 
 * for performance reasons. 
 * 
 * Internal only. 
 * 
 * @author jeremychone
 * @since  2.0.0
 *
 */
@Singleton
public class RestRegistry {
    
    private Map<String,WebRestRef> webGetRefByPath = new HashMap<String, WebRestRef>();
    private Map<String,WebRestRef> webPostRefByPath = new HashMap<String, WebRestRef>();
    private Map<String,WebRestRef> webPutRefByPath = new HashMap<String, WebRestRef>();
    private Map<String,WebRestRef> webDeleteRefByPath = new HashMap<String, WebRestRef>();
    
    private Map<Pattern,WebRestRef> webGetRefByVaredPath = new HashMap<Pattern, WebRestRef>();
    private Map<Pattern,WebRestRef> webPostRefByVaredPath = new HashMap<Pattern, WebRestRef>();
    private Map<Pattern,WebRestRef> webPutRefByVaredPath = new HashMap<Pattern, WebRestRef>();
    private Map<Pattern,WebRestRef> webDeleteRefByVaredPath = new HashMap<Pattern, WebRestRef>();    
    
    // --------- WebRef Getters --------- //
    public WebRestRef getWebRestRef(RequestContext rc){
        return getWebRestRef(rc,null);
    }
    
    public WebRestRef getWebRestRef(RequestContext rc,String resourcePath){
        if (resourcePath == null){
            resourcePath = rc.getResourcePath();
        }
        HttpMethod method = rc.getMethod();
        
        WebRestRef ref = null;
        // first, check if there is a perfect match. 
        Map<String,WebRestRef> refByPath = getRefByPath(method);
        if (refByPath != null){
            ref = refByPath.get(resourcePath);
        }
        
        if (ref == null){
            // TODO: check if there is a vared path matching
            Map<Pattern,WebRestRef> refByPattern = getRefByPattern(method);
            
            for (Pattern pattern : refByPattern.keySet()){
                Matcher matcher = pattern.matcher(resourcePath);
                if (matcher.matches()){
                    ref = refByPattern.get(pattern);
                    break;
                }
            }
        }
        
        return ref;
    }
    // --------- /WebRef Getters --------- //
    
    
    // --------- Register Methods (called in init only) --------- //
    public void registerWebRest(Class webClass, Method m, ParamDef[] paramDefs, HttpMethod method, String[] paths ){
        Map<String,WebRestRef> refByPath = getRefByPath(method);
        Map<Pattern,WebRestRef> refByPattern = getRefByPattern(method);
        
        if (refByPath != null){
            WebRestRef ref = new WebRestRef(webClass,m, paramDefs);
            // if now paths, in the .value() then take the methodName as the path
            if (paths.length == 0){
                refByPath.put("/" + ref.getHandlerMethod().getName(), ref);
            }else{
                for (String path : paths){
                    if (path.indexOf("{") > -1){
                        Pair<Pattern, Map<Integer, String>> patternAndMap = getPathPatternAndMap(path);
                        Pattern pattern = patternAndMap.getFirst();
                        WebRestRef varPathRef = new WebRestRef(webClass,m, paramDefs,pattern,patternAndMap.getSecond());
                        refByPattern.put(pattern,varPathRef);
                    }else{
                        refByPath.put(path, ref);
                    }
                    
                }     
            }
        }
    }
    
    
    // --------- /Register Methods (called in init only) --------- //
    private Pair<Pattern,Map<Integer,String>> getPathPatternAndMap(String path){
        
        // --- find the var names --- //
        final String RGX_FIND_VAR =  "\\{(.*?)\\}";
        
        Pattern varFinderPattern = Pattern.compile(RGX_FIND_VAR);
        Matcher varFinderMatcher = varFinderPattern.matcher(path);
        
        Map<Integer,String> paramNameByIdx = new HashMap<Integer, String>();
        
        int group_idx = 0;
        StringBuilder pathRegExSb = new StringBuilder("^");
        String str = path;
        int str_idx = 0;
        while (varFinderMatcher.find()) {
            String paramName = varFinderMatcher.group();
            paramName = paramName.substring(1,paramName.length() -1);
            paramNameByIdx.put(group_idx, paramName);
            pathRegExSb.append(regexEscape(str.substring(str_idx, varFinderMatcher.start())));
            pathRegExSb.append("(.*?)");
            str_idx = varFinderMatcher.end();
            group_idx++;
        }
        pathRegExSb.append(regexEscape(str.substring(str_idx)));
        pathRegExSb.append("$");
        
        String pathRegEx = pathRegExSb.toString();
        Pattern pathPattern = Pattern.compile(pathRegEx);
        
        return new Pair<Pattern,Map<Integer,String>>(pathPattern,paramNameByIdx);
    }
    
    static private String regexEscape(String str){
        return str.replace(".", "\\.");
    }    
    
    private Map<Pattern,WebRestRef> getRefByPattern(HttpMethod httpMethod){
        switch (httpMethod){
            case GET: 
                return webGetRefByVaredPath;
            case POST: 
                return webPostRefByVaredPath;
            case PUT:
                return webPutRefByVaredPath;
            case DELETE:
                return webDeleteRefByVaredPath;
            default: 
                return null;
        }        
    }
    
    private Map<String,WebRestRef> getRefByPath(HttpMethod httpMethod){
        switch (httpMethod){
            case GET: 
                return webGetRefByPath;
            case POST: 
                return webPostRefByPath;
            case PUT:
                return webPutRefByPath;
            case DELETE:
                return webDeleteRefByPath;
            default: 
                return null;
        }        
    }
    


    
}

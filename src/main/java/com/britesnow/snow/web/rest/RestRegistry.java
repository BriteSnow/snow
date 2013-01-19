package com.britesnow.snow.web.rest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.HttpMethod;
import com.britesnow.snow.web.handler.ParamDef;
import com.britesnow.snow.web.rest.annotation.WebDelete;
import com.britesnow.snow.web.rest.annotation.WebGet;
import com.britesnow.snow.web.rest.annotation.WebPost;
import com.britesnow.snow.web.rest.annotation.WebPut;

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
    
    // --------- WebRef Getters --------- //
    public WebRestRef getWebRestRef(RequestContext rc){
        return getWebRestRef(rc,null);
    }
    
    public WebRestRef getWebRestRef(RequestContext rc,String resourcePath){
        if (resourcePath == null){
            resourcePath = rc.getResourcePath();
        }
        HttpMethod method = rc.getMethod();
        
        switch (method){
            case GET: 
                return webGetRefByPath.get(resourcePath);
            case POST: 
                return webPostRefByPath.get(resourcePath);
            case PUT:
                return webPutRefByPath.get(resourcePath);
            case DELETE:
                return webDeleteRefByPath.get(resourcePath);
            default: 
                return null;
        }
    }
    // --------- /WebRef Getters --------- //
    
    
    // --------- Register Methods (called in init only) --------- //
    public void registerWebGet(Class webClass, Method m, ParamDef[] paramDefs, WebGet webGet ){
        registerWebRest(webGetRefByPath,webGet.value(),new WebRestRef(webClass,m, paramDefs,webGet));
    }

    public void registerWebPost(Class webClass, Method m, ParamDef[] paramDefs, WebPost webPost ){
        registerWebRest(webPostRefByPath,webPost.value(),new WebRestRef(webClass,m, paramDefs,webPost));
    }
    
    public void registerWebPut(Class webClass, Method m, ParamDef[] paramDefs, WebPut webPut ){
        registerWebRest(webPutRefByPath,webPut.value(),new WebRestRef(webClass,m, paramDefs,webPut));
    }
    
    public void registerWebDelete(Class webClass, Method m, ParamDef[] paramDefs, WebDelete webDelete ){
        registerWebRest(webDeleteRefByPath,webDelete.value(),new WebRestRef(webClass,m, paramDefs,webDelete));
    }    
    // --------- /Register Methods (called in init only) --------- //
    
    
    

    private void registerWebRest(Map<String,WebRestRef> refByPath,String[] paths, WebRestRef webRestRef){
        for (String path : paths){
            refByPath.put(path, webRestRef);
        }        
    }
    
    


}

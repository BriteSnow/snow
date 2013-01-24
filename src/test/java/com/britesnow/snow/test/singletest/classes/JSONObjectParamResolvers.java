package com.britesnow.snow.test.singletest.classes;

import javax.inject.Singleton;

import net.sf.json.JSONObject;

import com.britesnow.snow.util.AnnotationMap;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.britesnow.snow.web.param.resolver.annotation.WebParamResolver;


@Singleton
public class JSONObjectParamResolvers {

    @WebParamResolver()
    public JSONObject resolveJsonObject(AnnotationMap annotationMap, Class paramType, RequestContext rc){
        try {
            return JSONObject.fromObject( rc.getParam( annotationMap.get(WebParam.class).value() ) );
        } catch(Exception e) {
            return null;
        }        
    }
    
    
}

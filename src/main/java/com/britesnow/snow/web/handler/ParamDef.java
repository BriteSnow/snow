package com.britesnow.snow.web.handler;

import java.lang.annotation.Annotation;

import com.britesnow.snow.util.AnnotationMap;
import com.britesnow.snow.web.param.resolver.WebParamResolverRef;
import com.google.inject.Key;

public class ParamDef {

    private Class paramType;
    private Annotation[] annotations;
    private WebParamResolverRef webParamResolverRef;
    private Key key;
    
    
    private AnnotationMap annotationMap;
    private Annotation firstAnnotation;
    
    
    public ParamDef(Class paramType,Annotation[] annotations, WebParamResolverRef webParamResolverRef, Key key){
        this.paramType = paramType;
        this.annotations = annotations;
        this.webParamResolverRef = webParamResolverRef;
        this.key = key;
        
        annotationMap = new AnnotationMap(annotations);
        if (annotations != null && annotations.length > 0){
            firstAnnotation = annotations[0];
        }
    }
    
    public boolean hasWebParamResolver(){
        return (webParamResolverRef != null)?true:false;
    }

    public Class getParamType() {
        return paramType;
    }

    public Annotation firstAnnotation(){
        return firstAnnotation;
    }
    
    public Annotation[] getAnnotations() {
        return annotations;
    }
    
    public AnnotationMap getAnnotationMap(){
        return annotationMap;
    }

    public WebParamResolverRef getWebParamResolverRef() {
        return webParamResolverRef;
    }
    
    public Key getKey(){
        return key;
    }

    
}

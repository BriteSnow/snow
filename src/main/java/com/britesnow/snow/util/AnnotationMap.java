package com.britesnow.snow.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An AnnotationMap build a Map of Annotation by their Class name, and expose a Generic base get(annotationClass) to get the instance. 
 * 
 * @author jeremychone
 *
 */
public class AnnotationMap {

    Map<Class<? extends Annotation>, Annotation> annotationDic = new HashMap<Class<? extends Annotation>, Annotation>(); 
    
    public AnnotationMap(Annotation[] annotations){
        if (annotations != null){
            for (Annotation an : annotations){
                annotationDic.put(an.annotationType(),an);
            }
        }
    }
    
    public AnnotationMap(List<Annotation> annotations){
        if (annotations != null){
            for (Annotation an : annotations){
                annotationDic.put(an.getClass(),an);
            }
        }
    }
    
    public <T> T get(Class<T> annotationClass){
        return (T) annotationDic.get(annotationClass);
    }
}

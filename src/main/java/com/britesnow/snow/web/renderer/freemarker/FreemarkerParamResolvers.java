package com.britesnow.snow.web.renderer.freemarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.britesnow.snow.util.AnnotationMap;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.param.resolver.annotation.WebParamResolver;
import com.britesnow.snow.web.renderer.freemarker.annotation.FreemarkerMethodArguments;
import com.britesnow.snow.web.renderer.freemarker.annotation.FreemarkerParam;

import freemarker.core.Environment;

@Singleton
public class FreemarkerParamResolvers {
    
    @WebParamResolver(annotatedWith=FreemarkerParam.class)
    public Map resolveDirectiveParam(AnnotationMap annotationMap, Class paramType, RequestContext rc){
        return rc.getAttributeAs(FreemarkerDirectiveProxy.FREEMARKER_DIRECTIVE_CONTEXT, FreemarkerDirectiveContext.class).paramMap;
    }
    
    @WebParamResolver(annotatedWith=FreemarkerMethodArguments.class)
    public List resolveMethodArguments(AnnotationMap annotationMap, Class paramType, RequestContext rc){
        List values = new ArrayList();
        List freemarkerValues = rc.getAttributeAs(FreemarkerMethodProxy.FREEMARKER_METHOD_ARGUMENTS, List.class);
        for (Object freemarkerValue : freemarkerValues){
            values.add(FreemarkerUtil.getValue(freemarkerValue));
        }
        return values;
    }
    
    @WebParamResolver
    public Environment resolveEnvironment(AnnotationMap annotationMap, Class paramType, RequestContext rc){
        return rc.getAttributeAs(FreemarkerDirectiveProxy.FREEMARKER_DIRECTIVE_CONTEXT, FreemarkerDirectiveContext.class).env;
    }
}

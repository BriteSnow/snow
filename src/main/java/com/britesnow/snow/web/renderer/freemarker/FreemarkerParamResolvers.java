package com.britesnow.snow.web.renderer.freemarker;

import java.util.Map;

import javax.inject.Singleton;

import com.britesnow.snow.util.AnnotationMap;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.param.resolver.annotation.WebParamResolver;
import com.britesnow.snow.web.renderer.freemarker.annotation.FreemarkerParam;

import freemarker.core.Environment;

@Singleton
public class FreemarkerParamResolvers {
    
    @WebParamResolver(annotatedWith=FreemarkerParam.class)
    public Map resolveDirectiveParam(AnnotationMap annotationMap, Class paramType, RequestContext rc){
        return rc.getAttributeAs(TemplateDirectiveProxy.FREEMARKER_DIRECTIVE_CONTEXT, FreemarkerDirectiveContext.class).paramMap;
    }
    
    @WebParamResolver
    public Environment resolveEnvironment(AnnotationMap annotationMap, Class paramType, RequestContext rc){
        return rc.getAttributeAs(TemplateDirectiveProxy.FREEMARKER_DIRECTIVE_CONTEXT, FreemarkerDirectiveContext.class).env;
    }
}

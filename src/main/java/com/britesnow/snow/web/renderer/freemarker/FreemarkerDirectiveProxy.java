/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.renderer.freemarker;

import static com.britesnow.snow.web.renderer.freemarker.FreemarkerUtil.getDataModel;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.handler.FreemakerDirectiveHandlerRef;
import com.britesnow.snow.web.handler.MethodInvoker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class FreemarkerDirectiveProxy implements TemplateDirectiveModel {
    static private Logger logger = LoggerFactory.getLogger(FreemarkerDirectiveProxy.class);
    
    static final String FREEMARKER_DIRECTIVE_CONTEXT = "freemarkerDirectiveContext";
    
    private String name;
    private FreemakerDirectiveHandlerRef freemarkerDirectiveRef;
    
    @Inject
    private MethodInvoker methodInvoker;
    
    public FreemarkerDirectiveProxy(String name,FreemakerDirectiveHandlerRef webTemplateDirectiveRef){
        this.name = name;
        this.freemarkerDirectiveRef = webTemplateDirectiveRef;
    }

    public String getName(){
        return name;
    }
    
    public void execute(Environment env, Map paramMap, TemplateModel[] model, TemplateDirectiveBody body) throws TemplateException,
                            IOException {
        
        RequestContext rc = getDataModel("_r.rc", RequestContext.class);
        try {
            rc.setAttribute(FREEMARKER_DIRECTIVE_CONTEXT, new FreemarkerDirectiveContext(env, paramMap));
            methodInvoker.invokeWebHandler(freemarkerDirectiveRef, rc);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }finally{
            rc.removeAttribute(FREEMARKER_DIRECTIVE_CONTEXT);
        }
    }

}

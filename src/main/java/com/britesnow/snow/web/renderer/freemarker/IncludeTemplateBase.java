package com.britesnow.snow.web.renderer.freemarker;

import java.io.IOException;

import com.britesnow.snow.web.RequestContext;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

public class IncludeTemplateBase {
    
    protected void includeTemplate(RequestContext rc, String templatePath,Environment env) throws IOException, TemplateException{
        if (templatePath != null){
            env.include(templatePath, "UTF-8", true);
        }    	
    }
}

/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.renderer.freemarker;

import java.io.IOException;
import java.util.Map;


import com.britesnow.snow.web.RequestContext;
import com.google.inject.Singleton;


import static com.britesnow.snow.web.renderer.freemarker.FreemarkerUtil.getDataModel;
import static com.britesnow.snow.web.renderer.freemarker.FreemarkerUtil.getParam;


import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

@Singleton
public class IncludeTemplateDirective extends IncludeTemplateBase implements TemplateDirectiveModel {

    

    
    @Override
    public void execute(Environment env, Map args, TemplateModel[] arg2, TemplateDirectiveBody arg3)
                            throws TemplateException, IOException {

        String templateName = getParam(args,"name",String.class); 
        RequestContext rc = getDataModel("r.rc", RequestContext.class);
        
        includeTemplate(rc,templateName,env);
    }

}

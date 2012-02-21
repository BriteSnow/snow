/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.renderer.freemarker;


import java.util.List;


import com.britesnow.snow.util.FileUtil;
import com.britesnow.snow.web.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import static com.britesnow.snow.web.renderer.freemarker.FreemarkerUtil.getParam;

@Singleton
public class HrefPartTemplateMethod implements TemplateMethodModelEx {

    @Inject
    Application webApplication;
    @Inject
    CurrentRequestContextHolder currentRCHolder;

    @Override
    public Object exec(List args) throws TemplateModelException {

        String path = getParam(args.get(0),String.class);
        
        RequestContext rc = currentRCHolder.getCurrentRequestContext();
        
        String contextPath = rc.getContextPath();
        
        path = FileUtil.encodeFileName(path);
        
        String href = new StringBuilder(contextPath).append(path).toString();
        return href;
    }

}

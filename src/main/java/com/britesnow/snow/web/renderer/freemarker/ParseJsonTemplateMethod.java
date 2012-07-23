package com.britesnow.snow.web.renderer.freemarker;

import static com.britesnow.snow.web.renderer.freemarker.FreemarkerUtil.getDataModel;
import static com.britesnow.snow.web.renderer.freemarker.FreemarkerUtil.getParam;

import java.io.File;
import java.util.List;
import java.util.Map;


import com.britesnow.snow.util.FileUtil;
import com.britesnow.snow.util.JsonUtil;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.path.ResourceFileResolver;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

@Singleton
public class ParseJsonTemplateMethod implements TemplateMethodModelEx {

    @Inject
    private ResourceFileResolver pathFileResolver;
    
    @Override
    public Object exec(List args) throws TemplateModelException {
        Map result = null;
        String path = getParam(args.get(0), String.class);
        RequestContext rc = getDataModel("_r.rc", RequestContext.class);
        
        if (path != null) {
            
            File jsonFile = pathFileResolver.resolve(path,rc);
            
            if (jsonFile.exists()){
                String json = FileUtil.getFileContentAsString(jsonFile);
                result = JsonUtil.toMapAndList(json);
            }
        }
        
        return result;
        
    }

}

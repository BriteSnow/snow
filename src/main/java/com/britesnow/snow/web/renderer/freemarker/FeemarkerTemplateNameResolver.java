package com.britesnow.snow.web.renderer.freemarker;

import java.io.File;

import javax.inject.Singleton;


import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.binding.WebAppFolder;
import com.britesnow.snow.web.path.ResourceFileResolver;
import com.google.inject.Inject;



@Singleton
public class FeemarkerTemplateNameResolver {

    @Inject
    private @WebAppFolder File webAppFolder;
    
    @Inject
    private ResourceFileResolver resourceFileResolver;
    
    
    // resourcePath needs to be relative to WebAppFolder
    public String resolve(String resourcePath, RequestContext rc){
        if (resourcePath.endsWith("/")){
            resourcePath += "index";
        }
        if (!resourcePath.endsWith(".ftl")){
            resourcePath += ".ftl";
        }
        
        File resourceFile = resourceFileResolver.resolve(resourcePath, rc); //new File(webAppFolder,resourcePath);
        
        return getTemplateName(resourceFile);
    }
    
    private String getTemplateName(File resourceFile){
        if (resourceFile.exists()){
            String resourcePath = resourceFile.getAbsolutePath();
            //SystemOutUtil.printValue("FreemarkerPartProcessor.processPart resourcePath", resourcePath);
            File templateFile = new File(resourcePath);
            String templateName = resourcePath;
            //FIXME: THIS IS A HORRIBLE HACK (test if we have a windows naming convention C: or E: and remove it)
            if (resourcePath.length() > 2 && resourcePath.charAt(1) == ':') {
                templateName = templateFile.getPath().substring(2);
            }
            return templateName;            
        }else{
            return null;
        }        
    }    
}

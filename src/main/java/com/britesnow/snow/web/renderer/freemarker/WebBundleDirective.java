/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.renderer.freemarker;



import static com.britesnow.snow.web.renderer.freemarker.FreemarkerUtil.getDataModel;
import static com.britesnow.snow.web.renderer.freemarker.FreemarkerUtil.getParam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


import com.britesnow.snow.SnowException;
import com.britesnow.snow.util.ObjectUtil;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.path.ResourceFileResolver;
import com.britesnow.snow.web.path.ResourcePathResolver;
import com.britesnow.snow.web.renderer.WebBundleManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;



import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

@Singleton
public class WebBundleDirective implements TemplateDirectiveModel {
    public enum Alert{
        NOT_VALID_WEBBUNDLE_PATH, NOT_VALID_WEBBUNDLE_TYPE;
    }
    
    
    
    private enum LinkType {
        css, js, less
    };

    @Inject
    private WebBundleManager webBundleManager;
    @Inject
    private ResourceFileResolver pathFileResolver;
    @Inject
    private ResourcePathResolver resourcePathResolver;
    
    
    @Override
    public void execute(Environment env, Map args, TemplateModel[] tms, TemplateDirectiveBody body)
                            throws TemplateException, IOException {

        RequestContext rc = getDataModel("_r.rc", RequestContext.class);
        
        Boolean debug_links = rc.getParamAs(WebBundleManager.DEBUG_LINK_STRING, Boolean.class, null);
        //if not null, make sure we set the cookie with the value
        if (debug_links != null){
            rc.setCookie(WebBundleManager.DEBUG_LINK_STRING, debug_links);
        }
        //if there is not debug_link param in the URL, check the cookie (set false if not found)
        else{
            debug_links = rc.getCookie(WebBundleManager.DEBUG_LINK_STRING, Boolean.class, false);
        }
        
        String contextPath = rc.getContextPath();
        
        
        // [@webBundle folder="/css/" type="js" /]
        //get the param
        String path = getParam(args,"path",String.class); 
        if (!path.endsWith("/")){
            path = path + "/";
        }
        String webPath = contextPath + path;
        String typeStr = getParam(args,"type",String.class);
        String key = getParam(args,"key",String.class);
        
        LinkType linkType = ObjectUtil.getValue(typeStr, LinkType.class, null);
        
        if (linkType == null){
            throw new SnowException(Alert.NOT_VALID_WEBBUNDLE_TYPE,"type",typeStr);
        }
        
        String fileExt = "." + linkType.name();

        BufferedWriter bw = new BufferedWriter(env.getOut());
        
        //Part part = webApplication.getPart(path);
        //File folder = part.getResourceFile();
        String resourcePath = resourcePathResolver.resolve(path, rc);
        File folder = pathFileResolver.resolve(resourcePath,rc);
        
        if (!folder.exists()){
            throw new SnowException(Alert.NOT_VALID_WEBBUNDLE_PATH,"path",folder.getAbsolutePath());
        }
        
        StringBuilder sb = new StringBuilder();
        
        //if debug mode, include all the files
        
        if (debug_links && linkType != LinkType.less){
            List<File> files = webBundleManager.getWebBundleFiles(folder, fileExt);
            for (File file : files){
                sb.append(buildHtmlTag(webPath + file.getName(), linkType));
            }
        }
        //if not debug mode, then, include the "_web_bundle_all..."
        else{
            //if there is no key, then the key is the latest type
            if (key == null){
                List<File> files = webBundleManager.getWebBundleFiles(folder, fileExt);
                Long lasttime = 0L;
                for (File file : files){
                    Long t = file.lastModified();
                    if (t > lasttime){
                        lasttime = t;
                    }
                }
                key = "" + lasttime;
            }
            
            StringBuilder sbHref = new StringBuilder(webPath).append("_web_bundle_all");
            // if we have a key, then add it.
            if (key.length() > 0){
               sbHref.append("__").append(key).append("__");  
            }
            sbHref.append(fileExt);
            
            sb.append(buildHtmlTag(sbHref.toString(),linkType));
            
            
        }
        
        bw.write(sb.toString());
        
        bw.flush();
    }

    private String buildHtmlTag(String href,LinkType type){
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case less:
                sb.append("<link type='text/css' href='");
                sb.append(href);
                sb.append("'  rel='stylesheet/less'  />\n");
                break;
            case css:
                sb.append("<link type='text/css' href='");
                sb.append(href);
                sb.append("'  rel='stylesheet'  />\n");
                break;
            case js:
                sb.append("<script type='text/javascript' src='");
                sb.append(href);
                sb.append("'></script>\n");
                break;
        }        
        return sb.toString();
    }

}

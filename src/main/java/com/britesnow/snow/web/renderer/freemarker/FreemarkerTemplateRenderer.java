package com.britesnow.snow.web.renderer.freemarker;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.britesnow.snow.web.binding.WebAppFolder;
import com.britesnow.snow.web.renderer.TemplateRenderer;
import com.google.inject.Inject;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;

@Singleton
public class FreemarkerTemplateRenderer implements TemplateRenderer {
    static private Logger logger = LoggerFactory.getLogger(FreemarkerTemplateRenderer.class);

    private Configuration                        conf   = new Configuration();
    
    @Inject(optional=true)
    private ServletContext servletContext;
    
    @Inject
    private @WebAppFolder File webAppFolder;
    
    
    @Inject
    private FeemarkerTemplateNameResolver templateNameResolver;
    
    @Inject
    private IncludeTemplateDirective             includeTemplateDirective;
    @Inject
    private IncludeFrameContentTemplateDirective includeFrameContentTemplateDirective;
    @Inject
    private MaxTemplateMethod                    maxTemplateMethod;
    @Inject
    private HrefPartTemplateMethod               hrefPartTemplateMethod;
    @Inject
    private ParseJsonTemplateMethod parseJsonTemplateMethod;
    @Inject
    private WebBundleDirective webBundleDirective;    
    
    public void init() {
        File rootFile = webAppFolder.getAbsoluteFile();
        while (rootFile.getParentFile() != null) {
            rootFile = rootFile.getParentFile();
        }
        
        MultiTemplateLoader mtl;
        TemplateLoader[] loaders = null;

        // ////////// Set the templateLoaders ////////////////
        // if we have a servletContext, then include a webappTemplateLoader
        try {
            if (servletContext != null) {

                WebappTemplateLoader webappTemplateLoader = new WebappTemplateLoader(servletContext);

                TemplateLoader tl = new FileTemplateLoader(rootFile, true);
                loaders = new TemplateLoader[] { tl, webappTemplateLoader };

            } else {
                TemplateLoader tl = new FileTemplateLoader(rootFile, true);
                loaders = new TemplateLoader[] { tl };
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        if (loaders != null) {
            mtl = new MultiTemplateLoader(loaders);
            conf.setTemplateLoader(mtl);
        }

        // set the BeanWrapper
        conf.setObjectWrapper(BeansWrapper.DEFAULT_WRAPPER);
        // conf.set

        conf.setURLEscapingCharset("UTF-8");

        conf.setEncoding(Locale.US, "UTF-8");

        // now it will print "1000000" (and not "1,000,000")
        conf.setNumberFormat("0.######");

        // set the cache storage
        // conf.setCacheStorage(new MruCacheStorage(0,0));
        conf.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        conf.setWhitespaceStripping(true);

        conf.setSharedVariable("includeTemplate", includeTemplateDirective);
        
        conf.setSharedVariable("includeFrameContent", includeFrameContentTemplateDirective);

        // TODO: this needs to be deprecated
        conf.setSharedVariable("webBundle",webBundleDirective);

        conf.setSharedVariable("max", maxTemplateMethod);

        conf.setSharedVariable("hrefPart", hrefPartTemplateMethod);
        
        conf.setSharedVariable("parseJson",parseJsonTemplateMethod);

        conf.setSharedVariable("setHrefParam", new SetHrefParam());

        conf.setSharedVariable("piIs", new PathInfoMatcherTemplateMethod(PathInfoMatcherTemplateMethod.Mode.IS));

        conf.setSharedVariable("piStarts", new PathInfoMatcherTemplateMethod(PathInfoMatcherTemplateMethod.Mode.STARTS_WITH));
        
        // TODO: need to set the templateDirectiveProxy
        /*
        for (WebModule module : webApplication.getWebModules()) {
            for (TemplateDirectiveProxy directiveProxy : module.getTemplateDirectiveProxyList()) {
                conf.setSharedVariable(directiveProxy.getName(), directiveProxy);
            }
        }  
        */      
        
    }

    @Override
    public void render(String path, Object data, Writer out) {
        
        if (!(data instanceof Map)) {
            throw new RuntimeException("FreemarkerRenderer.processPart requires 'data' to be of type 'Map'. Current data type " + ((data != null) ? data.getClass()
                                    : "null"));
        }

        Map model = (Map) data;
        
        String templateName = templateNameResolver.resolve(path);
        
        if (templateName != null){
            try{
                Template template = conf.getTemplate(templateName);
                template.process(model, out);
            }catch(Exception e){
                logger.error("Error while rendering freemarker template " + path + " (" + templateName + ") because " + e.getMessage());
                e.printStackTrace();
            }
        }else{
            // DO nothing, if we are here, the flag ignoreTemplateNotFound was set to true, so, it is expected to silently ignore.
            logger.error("Template not found for path: " + path + " (" + templateName + ")");
            // TODO: need to decide a strategy here. 
        }
    }
    
    

}

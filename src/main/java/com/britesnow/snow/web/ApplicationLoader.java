/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.web.db.hibernate.DefaultHibernateModule;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * For Internal use, don't use. 
 * 
 * This Class is responsible to Load the application, meaning it will that it will create the Guice appInjector from the system 
 * Modules and the application modules. 
 * 
 * @author jeremychone
 *
 */
public class ApplicationLoader {
    static private Logger  logger = LoggerFactory.getLogger(ApplicationLoader.class);

    protected Injector     appInjector;
    // File sfkFolder;
    private ServletContext servletContext;
    private File           webAppFolder;

    PropertyPostProcessor  propertyPostProcessor;

    public ApplicationLoader(File webAppFolder, ServletContext servletContext) {
        this.webAppFolder = fixWebAppFolder(webAppFolder);
        this.servletContext = servletContext;

    }

    File getWebAppFolder() {
        return webAppFolder;
    }
    
    protected File getAppFolder(){
        File appFolder = null;
        if (servletContext != null) {
            String appDirPath = servletContext.getInitParameter("appDir");
            if (appDirPath != null) {
                appFolder = new File(appDirPath);
            } 
            
        }
        if (appFolder == null){
            appFolder = getWebAppFolder();
        }
        
        return appFolder;
    }

    @SuppressWarnings("unchecked")
    protected ApplicationLoader load() throws Exception {
        /*--------- Load WebApplication ---------*/
        // Building the root modules
        // rootModules cannot be overrided
        return load(null,(Module[])null);
        /*--------- /Load WebApplication ---------*/

    }
    
    /**
     * Protected so that we can subclass it in testsupport for programmatic configuration
     */
    protected ApplicationLoader load(Map<String,String> overrideProperties, Module... overrideModules){
        Map<String,String> appProperties = loadAppProperties();
        
        // override the properties if defined
        if (overrideProperties != null){
            for (String key : overrideProperties.keySet()){
                appProperties.put(key, overrideProperties.get(key));
            }
        }

      boolean hasHibernate = false;
      String hibernateFileProperty = appProperties.get("snow.hibernate.configuration");
      if (hibernateFileProperty != null) {
        Map<String, String> hibernateProperties = loadHibernateProperties(hibernateFileProperty);
        if (hibernateProperties.size() > 0) {
          hasHibernate = true;
          for (String key : hibernateProperties.keySet()) {
            appProperties.put(key, hibernateProperties.get(key));
          }
        }
      }
        
        // build the webApplicationModules from the properties
        String applicationConfigClassStr = appProperties.get("snow.webApplicationModules");
        Iterable<String> applicationClassNames = Splitter.on(",").split(applicationConfigClassStr);
        List<Module> appModules = buildApplicationModules(applicationClassNames);
        
        Module combinedAppModule = Modules.combine(appModules);
        if (overrideModules != null){
            combinedAppModule = Modules.override(combinedAppModule).with(overrideModules);
        }
        
        
        // build the rootModules
        List<Module> rootModules = new ArrayList<Module>();
        rootModules.add(new RootWebModule(servletContext));
        rootModules.add(new RootApplicationModule(appProperties, getWebAppFolder(), getAppFolder()));

        // get the applicationPackageBase
        String applicationPackageBase = null;
        if (appModules != null && appModules.size() > 0) {
            applicationPackageBase = appModules.get(0).getClass().getPackage().getName();
        }
        
        // build the defaultModules
        List<Module> defaultModules = new ArrayList<Module>();
        defaultModules.add(new DefaultApplicationModule(applicationPackageBase));

        if (hasHibernate || MapUtil.hasKeyStartsWith(appProperties, "hibernate.")) {
            defaultModules.add(new DefaultHibernateModule());
        }
        

        Module combinedDefaultAndAppModule;
        if (combinedAppModule != null) {
            combinedDefaultAndAppModule = Modules.override(defaultModules).with(combinedAppModule);
        } else {
            combinedDefaultAndAppModule = Modules.combine(defaultModules);
        }

        rootModules.add(combinedDefaultAndAppModule);

        appInjector = Guice.createInjector(rootModules);        
        
        
        return this;
    }

    public WebController getWebController() {
        return appInjector.getInstance(WebController.class);
    }

    
    protected List<Module> buildApplicationModules(Iterable<String> applicationModuleClassNames){
        List<Module> modules = new ArrayList<Module>();
        
        for (String className : applicationModuleClassNames){
            try {
                Class applicationModuleClass = Class.forName(className);
                Module applicationModule = (Module) applicationModuleClass.newInstance();
                modules.add(applicationModule);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
        
        return modules;
    }
    
    // protected so that we can call it in the Mock
    protected Map loadAppProperties() {
        // First load the application.properties
        Map appProperties = new Properties();
        File propertiesFile = getWebInfPropertiesFile();

        if (propertiesFile.exists()) {
            try {
                ((Properties)appProperties).load(new FileReader(propertiesFile));
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        } else {
            logger.info("No application.properties found at " + propertiesFile.getAbsolutePath()
                                    + " - Starting blank application.");
        }

        // if a ServletContext, then look if there is a WebApp instance in the appDir parent folder
        // with the name [appDir].application.properties
        File appFolder = getAppFolder();
        if (servletContext != null) {
            String appFolderName = appFolder.getName();

            File appDirPropertiesFile = new File(appFolder.getParentFile(), appFolderName + ".properties");
            if (appDirPropertiesFile.exists()) {
                Properties appDirProperties = new Properties();
                try {
                    appDirProperties.load(new FileReader(appDirPropertiesFile));
                } catch (Exception e) {
                    logger.error("Unable to load application properties", e);
                }
                // override the appProperties with the WebAppRoperties
                appProperties.putAll(appDirProperties);
            }

        }

        PropertyPostProcessor propertyPostProcessor = getPropertyPostProcessor();

        // if we do not have it programmatically, then, look in the
        // snow.propertyPostProcessorClass properties
        if (propertyPostProcessor == null) {
            String propertyPostProcessorClassName = (String) appProperties.get("snow.propertyPostProcessorClass");
            if (propertyPostProcessorClassName != null) {
                try {
                    Class<PropertyPostProcessor> propertyPostProcessorClass = (Class<PropertyPostProcessor>)
                        Class.forName(propertyPostProcessorClassName);

                    if (propertyPostProcessorClass != null) {
                        propertyPostProcessor = propertyPostProcessorClass.newInstance();
                    }
                } catch (Exception e) {
                    logger.error("Cannot load or process the PropertyPostProcess class: " +
                                 propertyPostProcessorClassName, e);
                }
            }
        }
        try {
            if (propertyPostProcessor != null) {
                appProperties = propertyPostProcessor.processProperties(appProperties);
            }
        } catch (Exception e) {
            logger.error("Cannot process PropertyPostProcess class: " + propertyPostProcessor, e);
        }

        return appProperties;
    }

    // --------- PropertyPostProcessor Methods --------- //

    PropertyPostProcessor getPropertyPostProcessor() {
        return propertyPostProcessor;
    }

    /**
     * Allow to programatically set a propertyPostProcessor to the appLoader. Usually used for Unit Testing.
     * 
     * @param propertyPostProcessor PropertyPostProcessor
     */
    void setPropertyPostProcessor(PropertyPostProcessor propertyPostProcessor) {
        this.propertyPostProcessor = propertyPostProcessor;
    }

    // --------- /PropertyPostProcessor Methods --------- //

    private File getWebInfPropertiesFile() {
        File webAppFolder = getWebAppFolder();
        return new File(webAppFolder, "WEB-INF/snow.properties");
    }

    private File fixWebAppFolder(File webAppFolder) {
        String webAppFolderName = webAppFolder.getName();
        // Linux hack (somehow on Linux when contextPath empty, the
        // webAppFolder.getName() return ".", so, if this
        // is the case, need to go one parent up
        if (".".equals(webAppFolderName)) {
            webAppFolder = webAppFolder.getParentFile();
        }

        return webAppFolder;
    }

  private Map<String, String> loadHibernateProperties(String hibernateFileProperty) {
    Map<String, String> properties = new HashMap<String, String>();
    InputStream stream = getResourceAsStream(hibernateFileProperty);
    if (stream != null) {
      try {
        SAXReader reader = new SAXReader();
        Document doc = reader.read(stream);
        Element sfNode = doc.getRootElement().element("session-factory");
        Iterator itr = sfNode.elementIterator("property");
        while (itr.hasNext()) {
          Element node = (Element) itr.next();
          String name = node.attributeValue("name");
          String value = node.getText().trim();
          if (!name.startsWith("hibernate")) {
            name = "hibernate." + name;
          }
          properties.put(name, value);
        }
        stream.close();
      }
      catch (DocumentException e) {
        logger.error("Unable to parse hibernate properties", e);
      }
      catch (IOException e) {
        logger.error("Unable to parse hibernate properties", e);
      }
    }
    return properties;
  }

  private InputStream getResourceAsStream(String resource) {
    String stripped = resource.startsWith("/") ? resource.substring(1) : resource;
    InputStream stream = null;
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader != null) {
      stream = classLoader.getResourceAsStream(stripped);
    }
    if (stream == null) {
      stream = ApplicationLoader.class.getResourceAsStream(resource);
    }
    if (stream == null) {
      stream = ApplicationLoader.class.getClassLoader().getResourceAsStream(stripped);
    }
    if (stream == null) {
      logger.error(resource + " not found");
    }
    return stream;
  }
}
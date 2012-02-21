/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.web.db.hibernate.DefaultHibernateModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class ApplicationLoader {
    static private Logger logger = LoggerFactory.getLogger(ApplicationLoader.class);

    protected Injector              appInjector;
    //File                  sfkFolder;
    private ServletContext        servletContext;
    private File                  webAppFolder; 

    PropertyPostProcessor propertyPostProcessor;

    public ApplicationLoader(File webAppFolder, ServletContext servletContext) {
        this.webAppFolder = fixWebAppFolder(webAppFolder);
        this.servletContext = servletContext;

    }

    public File getWebAppFolder(){
        return webAppFolder;
    }
    
    @SuppressWarnings("unchecked")
    public ApplicationLoader load() throws Exception {

        /*--------- Load the Properties ---------*/
        // First load the application.properties
        Properties appProperties = new Properties();
        File propertiesFile = getWebInfPropertiesFile();

        if (propertiesFile.exists()) {
            appProperties.load(new FileReader(propertiesFile));
        } else {
            logger.info("No application.properties found at " + propertiesFile.getAbsolutePath()
                                    + " - Starting blank application.");
        }

        // if a ServletContext, then look if there is a WebApp instance in the appDir parent folder
        // with the name [appDir].application.properties
        // The appDir is either the webApp root or the appDir that has been set in the servletContext initParameter
        // (usually by snowServlet)
        File appDir = null;
        if (servletContext != null) {
            String appDirPath = servletContext.getInitParameter("appDir");
            if (appDirPath != null) {
                appDir = new File(appDirPath);
            } else {
                appDir = getWebAppFolder();
            }

            String appDirFolderName = appDir.getName();

            File appDirPropertiesFile = new File(appDir.getParentFile(), appDirFolderName + ".application.properties");
            if (appDirPropertiesFile.exists()) {
                Properties appDirProperties = new Properties();
                appDirProperties.load(new FileReader(appDirPropertiesFile));
                // override the appProperties with the WebAppRoperties
                appProperties.putAll(appDirProperties);
            }

        }else{
            appDir = getWebAppFolder();
        }

        PropertyPostProcessor propertyPostProcessor = getPropertyPostProcessor();

        // if we do not have it programmatically, then, look in the
        // snow.snow.propertyPostProcessorClass properties
        if (propertyPostProcessor == null) {
            String propertyPostProcessorClassName = appProperties.getProperty("snow.propertyPostProcessorClass");
            if (propertyPostProcessorClassName != null) {
                try {
                    Class<PropertyPostProcessor> propertyPostProcessorClass = (Class<PropertyPostProcessor>) Class.forName(propertyPostProcessorClassName);

                    if (propertyPostProcessorClass != null) {
                        propertyPostProcessor = propertyPostProcessorClass.newInstance();
                    }
                } catch (Exception e) {
                    logger.error("Cannot load or process the PropertyPostProcess class: " + propertyPostProcessorClassName
                                            + "\nException: "
                                            + e.getMessage());
                }
            }
        }
        try {
            if (propertyPostProcessor != null) {
                appProperties = propertyPostProcessor.processProperties(appProperties);
            }
        } catch (Exception e) {
            logger.error("Cannot process PropertyPostProcess class: " + propertyPostProcessor
                                    + "\nException: "
                                    + e.getMessage());
        }

        /*--------- /Load the Properties ---------*/

        /*--------- Load WebApplication ---------*/
        // Building the root modules
        // rootModules cannot be overrided
        List<Module> rootModules = new ArrayList<Module>();
        rootModules.add(new RootWebModule(servletContext));
        rootModules.add(new RootApplicationModule(appProperties, getWebAppFolder(), appDir));

        
        String applicationConfigClassStr = appProperties.getProperty("snow.applicationWebModuleConfigClass");
        Class applicationModuleClass = null;
        String applicationPackageBase = null;
        if (applicationConfigClassStr != null){
            applicationModuleClass = Class.forName(applicationConfigClassStr);
            applicationPackageBase = applicationModuleClass.getPackage().getName();
        }
        // build the default modules
        // default modules can be overrided
        List<Module> defaultModules = new ArrayList<Module>();
        defaultModules.add(new DefaultApplicationModule(applicationPackageBase));

        boolean hasHibernate = (appProperties != null && MapUtil.hasKeyStartsWith(appProperties, "hibernate."));
        if (hasHibernate) {
            defaultModules.add(new DefaultHibernateModule());
        }

        
        Module combineAppModule;
        if (applicationModuleClass != null) {
            Module applicationModule = (Module) applicationModuleClass.newInstance();
            combineAppModule = Modules.override(defaultModules).with(applicationModule);
        } else {
            combineAppModule = Modules.combine(defaultModules);
        }

        rootModules.add(combineAppModule);

        appInjector = Guice.createInjector(rootModules);
        /*--------- /Load WebApplication ---------*/

        return this;
    }

    
    public WebController getWebController(){
        return appInjector.getInstance(WebController.class);
    }
    
    // --------- PropertyPostProcessor Methods --------- //

    public PropertyPostProcessor getPropertyPostProcessor() {
        return propertyPostProcessor;
    }

    /**
     * Allow to programatically set a propertyPostProcessor to the appLoader. Usually used for Unit Testing.
     * 
     * @param propertyPostProcessor
     */
    public void setPropertyPostProcessor(PropertyPostProcessor propertyPostProcessor) {
        this.propertyPostProcessor = propertyPostProcessor;
    }

    // --------- /PropertyPostProcessor Methods --------- //
    
    private File getWebInfPropertiesFile(){
        File webAppFolder = getWebAppFolder();
        return new File(webAppFolder,"WEB-INF/snow/application.properties");
    }
    
    private File fixWebAppFolder(File webAppFolder) {
        String webAppFolderName = webAppFolder.getName();
        // Linux hack (somehow on Linux when contextPath empty, the
        // webAppFolder.getName() return ".", so, if this
        // is the case, need to go one parent up
        if (".".equals(webAppFolderName)) {
            webAppFolder = webAppFolder.getParentFile();
            webAppFolderName = webAppFolder.getName();
        }

        return webAppFolder;
    }    
}
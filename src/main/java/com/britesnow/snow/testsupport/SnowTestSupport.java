package com.britesnow.snow.testsupport;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;

import com.britesnow.snow.testsupport.mock.ApplicationLoaderMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory;
import com.britesnow.snow.web.WebController;
import com.google.inject.Injector;
import com.google.inject.Module;

public class SnowTestSupport {
    protected static String       SNOW_FOLDER_SAMPLE1_PATH = "TOOVERRIDE";

    protected static ApplicationLoaderMock appLoader;
    protected static Injector appInjector; 
    protected static WebController webController;
    
    
    protected static RequestContextMockFactory requestContextFactory;
    
    
    /**
     * Initialize an application with a webAppFolder (this will go and read the /WEB-INF/snow/application.properties file to initialize the application)
     * Must be be called by the TestUnit class from the @BeforeClass method
     * @param webAppFolderStr
     */
    public static void initWebApplication(String webAppFolderStr) throws Exception {
        initWebApplication(webAppFolderStr, null, (Module[])null);
    }
    
    public static void initWebApplication(String webAppFolderStr, Module... applicationModules) throws Exception{
        initWebApplication(webAppFolderStr,null,applicationModules);
    }

    public static void initWebApplication(String webAppFolderStr, Map properties) throws Exception{
        initWebApplication(webAppFolderStr,properties);
    }
    
    /**
     * Same as  initWebApplication, but with all the overrides
     */
    public static void initWebApplication(String webAppFolderStr,Map properties, Module... applicationModules) throws Exception{
        File webappFolder = new File(webAppFolderStr);
        
        assertTrue("WebApp Folder " + webappFolder.getAbsolutePath() + " does not exist", webappFolder.exists());
        
        
        // load this application
        appLoader = new ApplicationLoaderMock(webappFolder).loadWithOverrides(properties,applicationModules);
        
        // init the application
        webController = appLoader.getWebController();
        webController.init();

        // for convenience get the appInjector
        appInjector = appLoader.getApplicationInjector();
        
        // for convenience create a RequestContextMockFactory
        requestContextFactory = new RequestContextMockFactory();       
    }

    @AfterClass
    public static void shutdownWebApplicaton() throws Exception {
        // to allow other to call this shutdownWebApplicaton
        if (webController != null){
            webController.destroy();
            appInjector = null;
            webController = null;
            appLoader = null;
        }
    }

    
    // --------- Assert --------- //
    public void assertContains(String[] contains,String result){
        for (String contain : contains){
            if (result.indexOf(contain) < 0){
                Assert.fail("Result did not contain [" + contain + "] but was:\n" + result);
            }
        }
    }
    // --------- /Assert --------- //


}

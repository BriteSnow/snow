package com.britesnow.snow.testsupport;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;

import com.britesnow.snow.testsupport.mock.ApplicationLoaderMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory;
import com.britesnow.snow.web.WebController;
import com.google.inject.Injector;
import com.google.inject.Module;

public class SnowWebApplicationTestSupport {
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
        File webappFolder = new File(webAppFolderStr);
        
        assertTrue("WebApp Folder " + webappFolder.getAbsolutePath() + " does not exist", webappFolder.exists());

        // load this application
        appLoader = new ApplicationLoaderMock(webappFolder).load();
        
        // init the application
        webController = appLoader.getWebController();
        webController.init();

        // for convenience get the appInjector
        appInjector = appLoader.getApplicationInjector();
        
        // for convenience create a RequestContextMockFactory
        requestContextFactory = new RequestContextMockFactory();
    }
    
    /**
     * Alternative way to load an application by setting directly the applicationModules and properties
     * Must be called by the TestUnit class from the @BeforeClass method
     */
    public static void initWebApplication(String webAppFolderStr,List<Module> applicationModules,Map properties) throws Exception{
        File webappFolder = new File(webAppFolderStr);
        assertTrue("WebApp Folder " + webappFolder.getAbsolutePath() + " does not exist", webappFolder.exists());
        
        appLoader = new ApplicationLoaderMock(webappFolder).load(applicationModules,properties);
        
    }

    @AfterClass
    public static void releaseWebApplicaton() throws Exception {
        ////not supported yet
        webController.destroy();
        appInjector = null;
        webController = null;
        appLoader = null;
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

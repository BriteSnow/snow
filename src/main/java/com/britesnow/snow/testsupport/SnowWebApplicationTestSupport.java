package com.britesnow.snow.testsupport;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.AfterClass;

import com.britesnow.snow.testsupport.mock.ApplicationLoaderMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory;
import com.britesnow.snow.web.WebController;
import com.google.inject.Injector;

public class SnowWebApplicationTestSupport {
    protected static String       SNOW_FOLDER_SAMPLE1_PATH = "TOOVERRIDE";

    protected static ApplicationLoaderMock appLoader;
    protected static Injector appInjector; 
    protected static WebController webController;
    
    
    protected static RequestContextMockFactory requestContextFactory;
    
    
    /**
     * But be called by the TestUnit class from the @BeforClass method
     * @param appFolderStr
     * @throws Exception
     */
    public static void initWebApplication(String appFolderStr) throws Exception {
        File sfkFolder = new File(appFolderStr);
        
        assertTrue("Snow Folder " + sfkFolder.getAbsolutePath() + " does not exist", sfkFolder.exists());

        // load thie application
        appLoader = new ApplicationLoaderMock(sfkFolder, null).load();
        
        // init the application
        webController = appLoader.getWebController();
        webController.init();

        // for convenience get the appInjector
        appInjector = appLoader.getApplicationInjector();
        
        // for convenience create a RequestContextMockFactory
        requestContextFactory = new RequestContextMockFactory().init();
    }

    @AfterClass
    public static void releaseWebApplicaton() throws Exception {
        ////not supported yet
        webController.destroy();
        appInjector = null;
        webController = null;
        appLoader = null;
    }



}

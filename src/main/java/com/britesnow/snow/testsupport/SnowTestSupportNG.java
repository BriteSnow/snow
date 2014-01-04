package com.britesnow.snow.testsupport;

import com.britesnow.snow.testsupport.mock.ApplicationLoaderMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory;
import com.britesnow.snow.web.WebController;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.testng.annotations.AfterClass;

import java.io.File;
import java.util.Map;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
/**
 * @author Imran Zahid
 *         date 1/3/14
 */
public class SnowTestSupportNG {
  protected static ApplicationLoaderMock appLoader;
  protected static Injector appInjector;
  protected static WebController webController;
  protected static RequestContextMockFactory requestContextFactory;

  /**
   * Initialize an application with a webAppFolder
   * (this will go and read the /WEB-INF/snow/application.properties
   * file to initialize the application)
   * Must be be called by the TestUnit class from the @BeforeClass method
   * @param webAppFolderStr Folder name to initialize an application with
   */
  public static void initWebApplication(String webAppFolderStr) throws Exception {
    initWebApplication(webAppFolderStr, null, (Module[])null);
  }

  public static void initWebApplication(String webAppFolderStr, Module... applicationModules) throws Exception {
    initWebApplication(webAppFolderStr, null, applicationModules);
  }

  public static void initWebApplication(String webAppFolderStr, Map properties) throws Exception {
    initWebApplication(webAppFolderStr, properties, (Module[])null);
  }

  /**
   * Same as  initWebApplication, but with all the overrides
   */
  public static void initWebApplication(String webAppFolderStr, Map properties, Module... applicationModules)
      throws Exception{
    File webappFolder = new File(webAppFolderStr);

    assertTrue(webappFolder.exists(), "WebApp Folder " + webappFolder.getAbsolutePath() + " does not exist");

    // load this application
    appLoader = new ApplicationLoaderMock(webappFolder).loadWithOverrides(properties, applicationModules);

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
    if (webController != null) {
      webController.destroy();
      appInjector = null;
      webController = null;
      appLoader = null;
    }
  }

  // --------- Assert --------- //
  public void assertContains(String[] contains, String result) {
    for (String contain : contains) {
      if (!result.contains(contain)) {
        fail("Result did not contain [" + contain + "] but was:\n" + result);
      }
    }
  }
}

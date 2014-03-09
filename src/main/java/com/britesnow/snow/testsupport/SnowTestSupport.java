package com.britesnow.snow.testsupport;

import java.io.File;
import java.util.Map;

import com.britesnow.snow.testsupport.mock.RequestContextMock;

import com.britesnow.snow.testsupport.mock.ApplicationLoaderMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory;
import com.britesnow.snow.web.WebController;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * <p>Support class to build Snow Unit test.</p>
 *
 * <p>Note that this class is independent from any testing framework is designed to be extended into an Base Application Test Support class that would implement
 * the common test unit framework methods (e.g., @BeforeClass ...)</p>
 * <p>For example, in the case of JUnit, a typical usage would to create an ApplicationTestSupport subclass which initialize the application with the Junit @BeforeClass annotation:</p>
 * <pre>
 *public class ApplicationJUnitTestSupport extends SnowTestSupport{
 *  &#064;BeforeClass
 *  public static void initTestClass() throws Exception {
 *    SnowTestSupport.initWebApplication("src/main/webapp");
 *  }
 *}
 * </pre>
 */
public class SnowTestSupport {
    protected static String       SNOW_FOLDER_SAMPLE1_PATH = "TOOVERRIDE";

    protected static ApplicationLoaderMock appLoader;
    protected static Injector appInjector; 
    protected static WebController webController;
    
    
    protected static RequestContextMockFactory requestContextFactory;
    
	// --------- Web Application Initialization Utilities --------- //
	/**
	 * <p>Initialize an application with a webAppFolder (this will go and read the /WEB-INF/snow/application.properties file to initialize the application as it would
	 * be in servlet environment. Must be be called by the TestUnit class from the @BeforeClass method.</p>
	 * <p>Typical JUnit use is to create an ApplicationBaseTest support like:</p>
	 * @param webAppFolderStr The relative path to the source web app folder (for a maven setup this will be "src/main/webapp")
	 */
	public static void initWebApplication(String webAppFolderStr) throws Exception {
		initWebApplication(webAppFolderStr, null, (Module[])null);
	}

	/**
	 * <p>As the initWebApplication(webAppFolderStr) but allows to add some Guice modules that could override the default
	 * Guice application modules from the snow.properties.</p>
	 *
	 * @param webAppFolderStr
	 * @param applicationModules
	 * @throws Exception
	 */
	public static void initWebApplication(String webAppFolderStr, Module... applicationModules) throws Exception{
		initWebApplication(webAppFolderStr,null,applicationModules);
	}

	/**
	 * <p>As the initWebApplication(webAppFolderStr) but allows to add or override snow.properties default.</p>
	 *
	 * @param webAppFolderStr
	 * @param properties
	 * @throws Exception
	 */
	public static void initWebApplication(String webAppFolderStr, Map properties) throws Exception{
		initWebApplication(webAppFolderStr,properties,(Module[])null);
	}

	/**
	 * Full initWebApplication that allow to override the snow properties values as well as specifying override Guice modules.
	 *
	 * @param webAppFolderStr the relative path to the WebApp folder. Usually <em>src/main/webapp</em>
	 * @param properties
	 * @param applicationModules
	 */
	public static void initWebApplication(String webAppFolderStr,Map properties, Module... applicationModules) throws Exception{
		//make sure any eventual previous WebApplication
		shutdownWebApplication();

		File webappFolder = new File(webAppFolderStr);

		if (webappFolder.exists()){
			StringBuilder sb = new StringBuilder();
			sb.append("SnowTestSupport cannot be initialized because webAppFolder " + webAppFolderStr + " does not exists [" + webappFolder.getAbsolutePath() + "].");
		}

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

	public static void shutdownWebApplication() throws Exception {
		// to allow other to call this shutdownWebApplication
		if (webController != null){
			webController.destroy();
		}
		appInjector = null;
		webController = null;
		appLoader = null;

	}
	// --------- /Web Application Initialization Utilities --------- //

	// --------- Convenient Http Rest Mocks --------- //

	/**
	 * Convenient test method to execute a HTTP GET on this uri with those HTTP params. This will create the RequestContextMock,
	 * set the params, and simulate a HTTP Get to this URI via the WebController.service method (the same
	 * method used in a real servlet handling)
	 * @param uri
	 * @param params
	 * @return
	 */
	public RequestContextMock doGet(String uri, Map params){
		return doHttp(RequestContextMockFactory.RequestMethod.GET, uri, params,null);
	}

	public RequestContextMock doGet(String uri, Map params, Map cookies){
		return doHttp(RequestContextMockFactory.RequestMethod.GET, uri, params,cookies);
	}


	/**
	 * Same as doGet for a HTTP POST.
	 * @param uri
	 * @param params
	 * @return
	 */
	public RequestContextMock doPost(String uri, Map params){
		return doHttp(RequestContextMockFactory.RequestMethod.POST, uri, params,null);
	}

	public RequestContextMock doPost(String uri, Map params, Map cookies){
		return doHttp(RequestContextMockFactory.RequestMethod.POST, uri, params,cookies);
	}

	/**
	 * Same as doGet for a HTTP PUT.
	 * @param uri
	 * @param params
	 * @return
	 */
	public RequestContextMock doPut(String uri, Map params){
		return doHttp(RequestContextMockFactory.RequestMethod.PUT, uri, params,null);
	}

	public RequestContextMock doPut(String uri, Map params, Map cookies){
		return doHttp(RequestContextMockFactory.RequestMethod.PUT, uri, params, cookies);
	}

	/**
	 * Same as doGet for a HTTP DELETE.
	 * @param uri
	 * @return
	 */
	public RequestContextMock doDelete(String uri){
		return doHttp(RequestContextMockFactory.RequestMethod.DELETE, uri, null, null);
	}

	/**
	 * The lower level method of doGet and doPost which take the requestMethod (POST or GET).
	 *
	 * @param requestMethod
	 * @param uri
	 * @param params
	 * @return
	 */
	public RequestContextMock doHttp(RequestContextMockFactory.RequestMethod requestMethod, String uri, Map params, Map cookies){
		RequestContextMock rc = requestContextFactory.createRequestContext(requestMethod, uri);
		rc.setParamMap(params);
		rc.setCookieMap(cookies);
		webController.service(rc);
		return rc;
	}
	// --------- /Convenient Http Rest Mocks --------- //
}

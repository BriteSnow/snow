package com.britesnow.snow.test.apptest;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.inject.Inject;

import com.britesnow.snow.test.AssertUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.britesnow.snow.testsupport.SnowTestSupport;
import com.britesnow.snow.testsupport.mock.RequestContextMock;
import com.britesnow.snow.testsupport.mock.RequestContextMockFactory.RequestMethod;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.binding.WebAppFolder;
import com.britesnow.snow.web.path.ResourceFileLocator;
import com.google.inject.AbstractModule;

public class ResourceResolversTest extends SnowTestSupport {

    public final static String IGNORE_PREFIX = "_ignorethis_";

    @BeforeClass
    public static void initTestClass() throws Exception {
        SnowTestSupport.initWebApplication("src/test/resources/simpleApp", new AbstractModule() {

            @Override
            protected void configure() {
                bind(ResourceFileLocator.class).to(CustomResourceFileResolver.class);
            }
        });
    }

    @Test
    public void testResourceFileResolver() throws Exception {
        String result;
        RequestContextMock rc;

        // test 
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, IGNORE_PREFIX + "/notes/index");
        webController.service(rc);
        result = rc.getResponseAsString();
        assertEquals("---!!!notes/_frame!!!This is the notes/index.ftl page!!!/notes/_frame!!!---", result);
    }
    
    @Test
    public void testJsWebBundleResult() throws Exception{
        String result;
        RequestContextMock rc;        
        
        // test the get on a _web_bundle_all__.... request.
        rc = requestContextFactory.createRequestContext(RequestMethod.GET, IGNORE_PREFIX + "/js/_web_bundle_all__anykey__.js");
        webController.service(rc);
        result = rc.getResponseAsString();
        AssertUtils.assertContains(new String[]{"var js1", "var js2"}, result);
    }    

}

class CustomResourceFileResolver implements ResourceFileLocator {

    @Inject
    private @WebAppFolder
    File webAppFolder;

    public File locate(String resourcePath, RequestContext rc) {
        // incase there is teh ignore this prefix, remove it (note that sometime we will have // in the resourcePath,
        // but this works)
        resourcePath = resourcePath.replace(ResourceResolversTest.IGNORE_PREFIX, "");
        return new File(webAppFolder, resourcePath);
    }

}

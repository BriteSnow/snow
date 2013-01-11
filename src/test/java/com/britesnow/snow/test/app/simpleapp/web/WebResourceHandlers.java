package com.britesnow.snow.test.app.simpleapp.web;

import java.io.StringReader;

import javax.inject.Singleton;

import com.britesnow.snow.web.HttpWriter;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.handler.annotation.WebResourceHandler;
import com.britesnow.snow.web.param.annotation.WebPath;
import com.google.inject.Inject;

/**
 * This sample WebResourceHandlers that handle various files
 * @author jeremychone
 *
 */
@Singleton
public class WebResourceHandlers {

    @Inject
    HttpWriter httpWriter; 
    
    // Here we match any file with the .txt extension in the path /echotext 
    // (for example the url path /echotext/helloworld.txt will match this)
    @WebResourceHandler(matches="/echo1/([^\\s]+(\\.(?i)(txt|text))$)")
    public void handleEcho1Files(@WebPath(1)String fileName, RequestContext rc){
        httpWriter.writeStringContent(rc, fileName, new StringReader(fileName), true, null);
    }
    
    // Here is a simpler regex with an extension pre-filter
    @WebResourceHandler(matches="/echo2/.*",ext={".txt",".text"})
    public void handleEcho2Files(@WebPath(1)String fileName, RequestContext rc){
        httpWriter.writeStringContent(rc, fileName, new StringReader(fileName), true, null);
    } 
}

package com.britesnow.snow.test.app.simpleapp.web;

import java.util.List;

import javax.inject.Singleton;

import com.britesnow.snow.web.handler.annotation.FreemarkerDirectiveHandler;
import com.britesnow.snow.web.handler.annotation.FreemarkerMethodHandler;
import com.britesnow.snow.web.renderer.freemarker.annotation.FreemarkerMethodArguments;

@Singleton
public class FreemarkerHandlers {

    
    // Note: for now, this is not tested and officially supported. 
    @FreemarkerDirectiveHandler
    public String fooFunc(){
        System.out.println("TemplateDirectiveHandler fooFunc called");
        return "fooFunc returned a value";
    }
    
    @FreemarkerMethodHandler
    public Object echoMethod(@FreemarkerMethodArguments List args){
        Object arg0 = (args.size() > 0)?args.get(0):null;
        
        return arg0;
    }
    
}

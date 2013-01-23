package com.britesnow.snow.web.rest;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.britesnow.snow.web.HttpWriter;
import com.britesnow.snow.web.HttpWriterOptions;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.renderer.JsonRenderer;
import com.britesnow.snow.web.rest.annotation.WebSerializer;
import com.google.common.base.Throwables;

@Singleton
public class DefaultWebSerializers {

    @Inject
    HttpWriter httpWriter;
    
    @Inject
    JsonRenderer jsonRenderer;
    
    @WebSerializer("application/json")
    public boolean serializeJson(Object result, RequestContext rc){
        try {
            httpWriter.setHeaders(rc, rc.getResourcePath(), false, new HttpWriterOptions().setContentType("application/json"));
            if (result instanceof String){
                rc.getWriter().write((String)result);
            }else{
                jsonRenderer.render(result, rc.getWriter());
            }
            
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return true;
    }
}

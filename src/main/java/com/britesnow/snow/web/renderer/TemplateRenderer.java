package com.britesnow.snow.web.renderer;

import java.io.Writer;

import com.britesnow.snow.web.RequestContext;

public interface TemplateRenderer{
    
    public void render(String path, Object model, Writer out, RequestContext rc);
    
}
package com.britesnow.snow.web.renderer.freemarker;

import java.util.Map;

import freemarker.core.Environment;

public class FreemarkerDirectiveContext {

    final Environment env;
    final Map paramMap;
    
    public FreemarkerDirectiveContext(Environment env, Map paramMap){
        this.env = env;
        this.paramMap = paramMap;
    }
}

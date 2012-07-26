/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.renderer.freemarker;

import java.util.List;

import com.britesnow.snow.web.RequestContext;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;


public class PathInfoMatcherTemplateMethod implements TemplateMethodModelEx {
    
    public enum Mode{
        STARTS_WITH,IS;
    }

    private Mode mode;

    
    public PathInfoMatcherTemplateMethod(Mode mode){
        this.mode = mode;
    }
    
    @Override
    public Object exec(List args) throws TemplateModelException {
        // old way: resourcePath = FreemarkerUtil.getDataModel("_r.pathInfo",String.class);
        RequestContext rc = FreemarkerUtil.getDataModel("_r.rc",RequestContext.class);
        String resourcePath = rc.getResourcePath();
        
        String pathInfoMatch = FreemarkerUtil.getParamAs(args.get(0),String.class);
        
        /*--------- Get the eventual true/false values ---------*/
        String trueValue = null;
        String falseValue = null;
        if (args.size() > 1){
            trueValue = FreemarkerUtil.getParamAs(args.get(1),String.class);
            falseValue = ""; //by default the false value will be empty
        }
        
        if (args.size() > 2){
            falseValue = FreemarkerUtil.getParamAs(args.get(2),String.class);
        }
        /*--------- /Get the eventual true/false values ---------*/
        
        /*--------- Match the string ---------*/
        boolean match = false;
        switch(mode){
            case IS:
                if (resourcePath.equals(pathInfoMatch)){
                    match= true;
                }
                break;
            case STARTS_WITH:
                if (resourcePath.startsWith(pathInfoMatch)){
                    match = true;
                }
                break;
                
        }
        /*--------- /Match the string ---------*/
        
        /*--------- Return the value ---------*/
        if (trueValue != null){
            return match?trueValue:falseValue;
        }else{
            return match;
        }

    }

}

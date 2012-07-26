/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web.renderer.freemarker;

import static com.britesnow.snow.web.renderer.freemarker.FreemarkerUtil.getParamAs;

import java.util.List;

import com.google.inject.Singleton;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

@Singleton
public class MaxTemplateMethod implements TemplateMethodModelEx {

    @Override
    public Object exec(List args) throws TemplateModelException {
        
        String content = getParamAs(args.get(0),String.class);
        Integer max = getParamAs(args.get(1),Integer.class);
        
        if (content.length() < max){
            return content;
        }else{
            StringBuilder sb = new StringBuilder();
            if (content.length() > 4){
                sb.append(content.substring(0,max-3)).append("...");
            }else{
                sb.append(content.substring(0,max));
            }
            return sb.toString();
            
        }
    }

}

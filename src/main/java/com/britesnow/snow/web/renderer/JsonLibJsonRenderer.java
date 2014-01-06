package com.britesnow.snow.web.renderer;

import java.io.IOException;
import java.io.Writer;

import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import net.sf.json.util.PropertyFilter;

public class JsonLibJsonRenderer implements JsonRenderer{
    static private final String[] excludes = {"stackTrace","password"};
    
    static private final PropertyFilter ignoreNullFilter = new PropertyFilter(){

        @Override
        public boolean apply(Object source, String name, Object value) {
            if (value != null){
                return false;
            }else{
                return true;
            }
        }
        
    };

    public void render(Object data, Writer out) {
        String jsonString;

        if (data == null) {
            jsonString = "{}";
        } else {
            JsonConfig jsonConfig = new JsonConfig();
            jsonConfig.setExcludes(excludes);
            jsonConfig.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
            jsonConfig.setJsonPropertyFilter(ignoreNullFilter);
            Object jsObj = JSONSerializer.toJSON(data,jsonConfig);
            jsonString = jsObj.toString();
        }

        try {
            out.write(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } 
    }

}

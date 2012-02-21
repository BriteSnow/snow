package com.britesnow.snow.web.renderer;

import java.io.IOException;
import java.io.Writer;

import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

public class DefaultJsonRenderer{
    static private final String[] excludes = {"stackTrace"};

    public void render(Object data, Writer out) {
        String jsonString;

        if (data == null) {
            jsonString = "{}";
        } else {
            JsonConfig jsonConfig = new JsonConfig();
            jsonConfig.setExcludes(excludes);
            jsonConfig.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
            Object jsObj = JSONSerializer.toJSON(data,jsonConfig);
            jsonString = jsObj.toString();
            
        }

        try {
            out.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}

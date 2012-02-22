/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.util;


import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

// TODO: needs to make it an interface and Guice managed (i.e. JsonProcessor)
public class JsonUtil {

    public static String toJson(Object obj) {
        return toJson(obj,null);
    }
    
    public static String toJson(Object obj, String[] excludes) {
        JsonConfig c = new JsonConfig();
        if (excludes != null) {
            c.setExcludes(excludes);
        }
        c.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
        Object jsObj = JSONSerializer.toJSON(obj, c);
        
        String result = jsObj.toString();
        

        return result;
    }
    
    public static Map toMapAndList(String json){
        
        //JSONSerializer.toJava(json)
        
        JSONObject jsonObject = JSONObject.fromObject( json );
        
        // 2010-11-09: Not sure how to use the JSONObject to create only Map and List
        //             so, doing is by hand right now.
        
        return jsonObject;
    }
   
}

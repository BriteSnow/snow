package com.britesnow.snow.test.util;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.britesnow.snow.util.JsonUtil;


public class JsonUtilTest {

    
    @Test
    public void testJsonToMap(){
        String json = "{name=\"json\",bool:true,int:1,double:2.2,func:function(a){ return a; },array:[1,2],otherObj:{name='test'}}";  
        Map map = JsonUtil.toMapAndList(json);
        
        assertEquals("json",map.get("name"));
        assertEquals("test",((Map) map.get("otherObj")).get("name"));
        
        
    }
}

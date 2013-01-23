package com.britesnow.snow.web.rest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.britesnow.snow.web.rest.annotation.WebSerializer;

@Singleton
public class SerializerRegistry {

    private Map<String,WebSerializerRef> serializerRefByContentType = new HashMap<String, WebSerializerRef>();
    
    
    public WebSerializerRef getWebSerializerRef(String contentType){
        return serializerRefByContentType.get(contentType);
    }
    
    public void registerWebSerializer(Class webClass, Method m, WebSerializer webSerializer){
        WebSerializerRef ref = new WebSerializerRef(webClass,m);
        
        for (String contentType : webSerializer.value()){
            serializerRefByContentType.put(contentType,ref);
        }
    }
    
}

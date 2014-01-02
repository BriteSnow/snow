package com.britesnow.snow.test.app.simpleapp.web;

import static com.britesnow.snow.util.MapUtil.mapIt;

import java.util.Map;

import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.param.annotation.WebModel;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.britesnow.snow.web.rest.annotation.WebGet;
import com.google.inject.Singleton;
import org.apache.commons.fileupload.FileItem;

@Singleton
public class WebParamResolverTestWebHandlers {

    public enum TestEnum{
        foo,bar;
    }
    
    @WebModelHandler(startsWith="/testEnumWebParam")
    public void testEnumWebParam(@WebModel Map m, @WebParam("testenum") TestEnum testenum){
        m.put("result", testenum.name());
    }
    
    
    @WebGet("/testLongArrayWebParam")
    public Map testLongArrayWebParam(@WebParam("stringArrayLongs")Long[] stringArrayLongs){
        return mapIt("stringArrayLongs",stringArrayLongs);
    }

    @WebGet("/testFileItem")
    public void testFileItem(@WebParam("file1")FileItem file1){
        // NOTE: empty test right now, just to see if it get binded
        // TODO: need to implement the mock for files.
    }
}

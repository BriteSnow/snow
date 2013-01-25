package com.britesnow.snow.test.singletest.classes;

import java.util.Map;

import javax.inject.Singleton;

import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.web.param.annotation.PathVar;
import com.britesnow.snow.web.rest.annotation.WebGet;

@Singleton
public class PathVarWebRests {

    
    @WebGet("/get-item-{id}")
    public Map getItem(@PathVar("id")Long id){
        return MapUtil.mapIt("item-id",id);
    }
}

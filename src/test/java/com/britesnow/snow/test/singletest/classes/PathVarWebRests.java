package com.britesnow.snow.test.singletest.classes;

import java.util.Map;

import javax.inject.Singleton;

import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.web.param.annotation.PathVar;
import com.britesnow.snow.web.rest.annotation.WebGet;

@Singleton
public class PathVarWebRests {

//    			"api/{type}/{id}",
//                        "zzz",
//                        "aaa",
//                        "api/Task/{bigId}",
//                        "bb{id}",
//                        "aa{id}",
//                        "api/Task/{id}--",
//                        "bb",
//                        "api/Task/123",
//                        "bb/{id}"};


    @WebGet("/get-{entity}-{id}")
    public Map getEntityId(@PathVar("entity")String entity, @PathVar("id")String id){
        return MapUtil.mapIt("path", "/get-{entity}-{id}", "entity", entity, "id",id);
    }

    @WebGet("/get-{entity}-{id}--")
    public Map getEntityIdExtra(@PathVar("entity")String entity, @PathVar("id")String id){
        return MapUtil.mapIt("path", "/get-{entity}-{id}--", "entity", entity, "id",id);
    }


    @WebGet("/get-task-{id}")
    public Map getTaskId(@PathVar("id")Long id){
        return MapUtil.mapIt("path", "/get-task-{id}", "id",id);
    }

    @WebGet("/get-task-{id}--")
    public Map getTaskIdExtra(@PathVar("id")Long id){
        return MapUtil.mapIt("path", "/get-task-{id}--", "id",id);
    }


    @WebGet("/get-task-123123")
    public Map getEntityIdFix(){
        return MapUtil.mapIt("path", "/get-task-123123");
    }

}

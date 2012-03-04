package com.britesnow.snow.test.app.simpleapp.web;

import java.util.HashMap;
import java.util.Map;

import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.web.handler.annotation.WebActionHandler;
import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.param.annotation.WebModel;
import com.britesnow.snow.web.param.annotation.WebParam;

public class SimpleWebHandlers {

    Map<Long,Map> contactStore = new HashMap<Long, Map>();
    Long contactIdSeq = 1L;
    
    public SimpleWebHandlers(){
        contactStore.put(contactIdSeq, MapUtil.mapIt("id",contactIdSeq,"name","Mike"));
        contactIdSeq++;
        contactStore.put(contactIdSeq, MapUtil.mapIt("id",contactIdSeq,"name","Dylan"));
        contactIdSeq++;
    }
    
    @WebModelHandler(startsWith="/contact")
    public void contactPage(@WebModel Map m, @WebParam("id")Long contactId){
        Map contact = contactStore.get(contactId);
        m.put("contact",contact);
    }
    
    @WebActionHandler
    public Map addContact(@WebParam("name")String contactName){
        Map newContact = MapUtil.mapIt("id",contactIdSeq,"name",contactName);
        
        contactStore.put(contactIdSeq, MapUtil.mapIt("name",contactName));
        
        contactIdSeq++;
        
        return newContact;
        
    }
}

package com.britesnow.snow.web.handler;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("serial")
class WebObjectValidationExceptions extends RuntimeException {
    
    private List<WebObjectValidationException> webObjectExceptions = new ArrayList<WebObjectValidationException>();
    
    public WebObjectValidationExceptions(){
    }
    
    public void addWebException(WebObjectValidationException webObjectException){
        webObjectExceptions.add(webObjectException);
    }
    
    public boolean hasExceptions(){
        return (webObjectExceptions.size() > 0)?true:false;
    }
    
    public String getMessage(){
        StringBuilder sb = new StringBuilder();
        
        for (WebObjectValidationException ex : webObjectExceptions){
            sb.append(ex.getMessage()).append("\n");
        }
        
        return sb.toString();
    }
    
    
}

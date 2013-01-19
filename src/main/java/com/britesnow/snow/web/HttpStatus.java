package com.britesnow.snow.web;

public enum HttpStatus {
    NOT_FOUND(404);
    
    private HttpStatus(int code){
        this.code = code;
    }
    
    public int code(){
        return code;
    }
    
    private int code;
}

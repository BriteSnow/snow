package com.britesnow.snow.web;

@SuppressWarnings("serial")
public abstract class AbortException extends RuntimeException {

    private int code;

    public AbortException(String message){
        super(message);
    }
    
    public void setCode(int code){
        this.code = code;  
    }
    
    public int getCode(){
        return code;
    }
}

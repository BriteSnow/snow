/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web;

public class WebActionResponse {
    public enum Status{
        success,error;
    }
    
    public enum ErrorType{
        insufficient_privilege,exception, action_not_found;
    }
    
    private Status status = Status.success;
    private Object result;
    private Throwable error;
    private ErrorType errorType; 
    
    /**
     * Create a "success" WebAction response
     * @param result
     */
    public WebActionResponse(Object result){
        this.result = result;
    }
    
    
    /**
     * Create a error response from an exception.
     * @param error
     */
    public WebActionResponse(Throwable error){
        this.status = Status.error;
        this.error = error;
        this.errorType = ErrorType.exception;
    }
    
    public WebActionResponse(ErrorType errorType){
        this.errorType = errorType;
        this.status = Status.error;
    }
    
    /*--------- Getters ---------*/
    public final Object getResult() {
        return result;
    }

    public final Status getStatus() {
        return status;
    }
    
    public Throwable getError() {
        return error;
    }
    
    public ErrorType getErrorType(){
        return errorType;
    }
    
    public String getErrorMessage(){
        if (error != null){
            if (error.getCause() != null){
                return error.getCause().getMessage();
            }else{
                return error.getMessage();
            }
        }else{
            return null;
        }
    }
    /*--------- /Getters ---------*/



    
    
}

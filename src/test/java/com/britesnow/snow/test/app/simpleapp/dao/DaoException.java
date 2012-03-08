package com.britesnow.snow.test.app.simpleapp.dao;

public class DaoException extends RuntimeException {
    
    public enum Error{
        ENTITY_NOT_FOUND;
    }
    
    private Error error;
    private String message;
    
    public DaoException(Error error,String message){
        this.error = error;
        this.message = message;
    }
    
    @Override
    public String getMessage(){
        return error.name() + " " + message;
    }

}

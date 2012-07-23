package com.britesnow.snow.test.app.simpleapp.dao;

public class DaoException extends RuntimeException {
    
    /**
     * 
     */
    private static final long serialVersionUID = -8102985585954767305L;

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

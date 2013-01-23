package com.britesnow.snow.web;

@SuppressWarnings("serial")
public class AbortWithHttpStatusException extends AbortException {


    public AbortWithHttpStatusException(HttpStatus status) {
        this(status.code(), null);
    }

    public AbortWithHttpStatusException(HttpStatus status, String message) {
        this(status.code(), message);
    }
    
    public AbortWithHttpStatusException(int status) {
        this(status, null);
    }

    public AbortWithHttpStatusException(int status, String message) {
        super(message);
        this.setCode(status);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + " : " + getCode() + (getMessage() != null ? " - " + getMessage() : "");
    }
}

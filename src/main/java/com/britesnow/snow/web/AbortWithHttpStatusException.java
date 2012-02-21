package com.britesnow.snow.web;

@SuppressWarnings("serial")
public class AbortWithHttpStatusException extends RuntimeException {

    private int status;


    public AbortWithHttpStatusException(int status) {
        this(status, null);
    }

    public AbortWithHttpStatusException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " : " + status + (getMessage() != null ? " - " + getMessage() : "");
    }
}

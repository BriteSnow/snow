package com.britesnow.snow.web;

@SuppressWarnings("serial")
public class AbortWithHttpStatusException extends RuntimeException {

    public enum HttpStatus{
        NOT_FOUND(404);
        
        private HttpStatus(int code){
            this.code = code;
        }
        
        public int code(){
            return code;
        }
        
        private int code;
    }
    private int status;

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

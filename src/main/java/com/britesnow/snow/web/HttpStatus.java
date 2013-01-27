package com.britesnow.snow.web;

// https://developer.mozilla.org/en-US/docs/HTTP/HTTP_response_codes
public enum HttpStatus {
    
    // INFO
    CONTINUE(100),
    SWITCHING_PROTOCOL(101),
    // Successful
    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NO_CONTENT(204),
    RESET_CONTENT(205),
    PARTIAL_CONTENT(206),
    // Redirection
    MOVED_PERMENTLY(301),
    FOUND(302),
    // Client Error
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    REQUEST_TIMEOUT(408),
    // Server Error
    SERVER_ERROR(500),
    NOT_IMPLEMENTED(501), 
    SERVICE_UNAVAILABLE(503);
    
    private HttpStatus(int code){
        this.code = code;
    }
    
    public int code(){
        return code;
    }
    
    private int code;
}

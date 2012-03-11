package com.britesnow.snow.web;

public class HttpWriterOptions {

    private String contentType;
    private String characterEncoding;
    
    
    public String getContentType() {
        return contentType;
    }
    public HttpWriterOptions setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
    
    public String getCharacterEncoding() {
        return characterEncoding;
    }
    public HttpWriterOptions setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
        return this;
    }
    
    
}

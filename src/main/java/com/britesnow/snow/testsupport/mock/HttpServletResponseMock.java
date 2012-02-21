package com.britesnow.snow.testsupport.mock;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


public class HttpServletResponseMock implements HttpServletResponse {

    // will be inject in constructor
    private ServletOutputStreamMock outputStream;
    // will be built from outputStream in constructor
    private PrintWriter writer;
    
    @Inject
    public HttpServletResponseMock(ServletOutputStreamMock outputStream){
        this.outputStream = outputStream;
        this.writer = new PrintWriter(outputStream);
    }
    
    // --------- Mock Methods --------- //
    public String getResponseAsString(){
        return outputStream.toString();
    }
    
    public byte[] getResponseAsByteArray(){
        return outputStream.toByArray();
    }
    // --------- /Mock Methods --------- //
    
    
    @Override
    public void flushBuffer() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getBufferSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContentType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    @Override
    public boolean isCommitted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resetBuffer() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBufferSize(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCharacterEncoding(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setContentLength(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setContentType(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLocale(Locale arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addCookie(Cookie arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addDateHeader(String arg0, long arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addHeader(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addIntHeader(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean containsHeader(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String encodeRedirectURL(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String encodeRedirectUrl(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String encodeURL(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String encodeUrl(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendError(int arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendError(int arg0, String arg1) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendRedirect(String arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDateHeader(String arg0, long arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHeader(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setIntHeader(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStatus(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStatus(int arg0, String arg1) {
        // TODO Auto-generated method stub

    }

}

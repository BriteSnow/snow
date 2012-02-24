package com.britesnow.snow.web.less;

import java.net.URL;

public class LessOptions {

    private String charset;
    private boolean css;
    private URL less;

    public String getCharset() {
        if (charset == null) {
            return "UTF-8";
        }
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public URL getLess() {
        if (less == null) {
            return getClass().getClassLoader().getResource("META-INF/less.js");
        }
        return less;
    }

    public void setLess(URL less) {
        this.less = less;
    }

    public boolean isCss() {
        return css;
    }

    public void setCss(boolean css) {
        this.css = css;
    }

}
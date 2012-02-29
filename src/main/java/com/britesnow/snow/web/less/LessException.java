package com.britesnow.snow.web.less;

import java.util.List;

/**
* For now,code from https://github.com/asual/lesscss-engine/
* 
* TODO: needs to make LessException a JsException so that we can use it with all JS things. 
* 
* TODO: needs to make it runtime exception as well to match Snow philosophy
* 
**/
public class LessException extends Exception {

    private static final long serialVersionUID = 662552833197468936L;

    private String errorType;
    private String filename;
    private int line;
    private int column;
    private List<String> extract;

    public LessException() {
        super();
    }

    public LessException(String message) {
        super(message);
    }

    public LessException(String message, Throwable e) {
        super(message, e);
    }

    public LessException(String message, String errorType, String filename, int line, int column, List<String> extract) {
        super(message);
        this.errorType = errorType != null ? errorType : "LESS Error";
        this.filename = filename;
        this.line = line;
        this.column = column;
        this.extract = extract;
    }

    public LessException(Throwable e) {
        super(e);
    }

    @Override
    public String getMessage() {
        if (errorType != null) {
            String msg = String.format("%s: %s (line %s, column %s)", errorType, super.getMessage(), line, column);
            if (!(extract == null) && !extract.isEmpty()) {
                msg += " near";
                for (String l : extract) {
                    msg += "\n" + l;
                }
            }
            return msg;
        }

        return super.getMessage();
    }

    /**
     * Type of error as reported by less.js
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * Filename that error occured in as reported by less.js
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Line number where error occurred as reported by less.js or -1 if unknown.
     */
    public int getLine() {
        return line;
    }

    /**
     * Column number where error occurred as reported by less.js or -1 if unknown.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Lines around error as reported by less.js
     */
    public List<String> getExtract() {
        return extract;
    }

}
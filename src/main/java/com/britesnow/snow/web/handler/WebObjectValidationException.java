package com.britesnow.snow.web.handler;

@SuppressWarnings("serial")
class WebObjectValidationException extends Exception {
    
    public enum ERROR{
        NO_SINGLETON("'WebClass' %1$s must be annotated with @Singleton, because objects with @Web... are treated as singleton by Snow.");
        
        private String msg;

        public String getMsg(){
            return msg;
        }
        
        ERROR(String msg){
            this.msg = msg;
        }
    }

    private Class webClass;
    private ERROR error;
    
    public WebObjectValidationException(Class webClass, ERROR error){
        this.webClass = webClass;
        this.error = error;
    }
    
    public String getMessage(){
        return String.format(error.getMsg(), webClass.getSimpleName());
    }
}

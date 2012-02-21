package com.britesnow.snow.web.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.handler.annotation.WebExceptionHandler;
import com.britesnow.snow.web.param.WebParamRef;
import com.britesnow.snow.web.param.WebParameterParser;

public class WebExceptionHandlerRef extends BaseWebHandlerRef {
	@SuppressWarnings("unused")
	private WebExceptionHandler webExceptionHandler;
	
	private Class<? extends Throwable> exceptionClass;
	
	public WebExceptionHandlerRef(Object webHandler, Method method, Map<Class<? extends Annotation>,WebParameterParser> webParameterParserMap,
                                  WebExceptionHandler webExceptionHandler) {
		super(webHandler, method, webParameterParserMap);
		
		this.webExceptionHandler = webExceptionHandler;
		
		initWebParamRefs();
		
		//for now, 
		exceptionClass = webArgRefs.get(0).getArgClass();
		
	}
	
	public Class getThrowableClass(){
		return exceptionClass;
	}
	
    public void invokeWebExceptionHandler(Throwable e,RequestContext rc) throws Exception{
        Object[] paramValues = new Object[webArgRefs.size()];
        
        //the first param of a WebModel MUST be the Model Map
        paramValues[0] = e;
        
        if (webArgRefs.size() > 1){
            for (int i = 1; i < paramValues.length;i++){
                WebParamRef webParamRef = webArgRefs.get(i);
                paramValues[i] = webParamRef.getValue(method, rc);
            }
        }
        
        method.invoke(webHandler,paramValues);
    }	

}

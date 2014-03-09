package com.britesnow.snow.testsupport.mock;

import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import com.britesnow.snow.util.JsonUtil;
import com.britesnow.snow.util.ObjectUtil;
import com.britesnow.snow.web.HttpMethod;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.auth.AuthToken;

public class RequestContextMock extends RequestContext {

    HttpServletRequestMock req;
    HttpServletResponseMock res; 
                 
    
    /**
     * This should be call via the MockFactory 
     * @param req
     * @param res
     * @param servletContext
     */
    @Inject
    public RequestContextMock(HttpServletRequestMock req,HttpServletResponseMock res, ServletContext servletContext){
        super(req,res,servletContext,null);
        this.req = req;
        this.res = res;
    }
    
    
    public void setRequestMethod(String method){
        req.setMethod(method);
        httpMethod = ObjectUtil.getValue(req.getMethod(), HttpMethod.class, null);
    }

	/**
	 * <p>Mock method to set the AuthToken to be this user.</p>
	 * <p>Very usefull to test request flow without testing the authentication flow.</p>
	 * @param user
	 */
	public void setUser(Object user){
		super.setAuthToken(new AuthToken(user));
	}

	/**
	 * Mock method to inject the HTTP param to this RequestContextMock
	 * @param params
	 */
    public void setParamMap(Map params){
        super.setParamMap(params);
    }

	/**
	 * Mock method to inject the cookies to this RequestContextMock
	 * @param cookieMap
	 */
	public void setCookieMap(Map cookieMap){
		super.setCookieMap(cookieMap);
	}

    public void setPathInfo(String pathInfo){
        req.setPathInfo(pathInfo);
    }


    public String getResponseAsString(){
        return res.getResponseAsString();
    }
    
    public Map getResponseAsJson(){
        String response  = getResponseAsString();
        return JsonUtil.toMapAndList(response);
    }
    
    public byte[] getResponseAsByArray(){
        return res.getResponseAsByteArray();
    }
    
    public void init(){
        super.init();
    }
}

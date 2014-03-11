/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.britesnow.snow.util.HttpRequestUtil;
import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.util.ObjectUtil;
import com.britesnow.snow.web.auth.AuthToken;
import com.google.common.base.Throwables;

public class RequestContext {
    static private Logger       logger             = LoggerFactory.getLogger(RequestContext.class);

    private HttpServletRequest  req;
    private HttpServletResponse res;
    ServletContext              servletContext;

    private ServletFileUpload   fileUploader;
    private boolean             isParamInitialized = false;
    private boolean             isMultipart        = false;
    private List<?>             fileItems;

    // built on demand
    private Map<String, Object> paramMap           = null;

    private Map<String, String> pathVarMap         = null;

    // built on demand
    private Map<String, String> cookieMap;

    private String              resourcePath;
    private Deque<String>       framePathsStack;

    // this is the HashMap for the "m" model map
    private Map                 webModel           = new HashMap();

    // usually set by the WebController.service
    private WebActionResponse   webActionResponse;

    // optional
    private String              pathInfo;

    // set by AuthService.authRequest
    private AuthToken<?>        authToken;

    // rest response contentType (for now used by the Rest api, and defined by ContentTypeResolver)
    private String              restContentType;

    // protected to allow mock override
    protected HttpMethod        httpMethod;

    private WebRequestType      webRequestType;

    private Object              result;

	private HashMap<Class,Object> singletonDataMap = new HashMap<Class,Object>();

	private HashMap<String, Object> namedDataMap = new HashMap<String, Object>();

    // mostly useful for unit testing
    private AbortException      abortException;

    public RequestContext(HttpServletRequest req, HttpServletResponse res, ServletContext servletContext,
                            ServletFileUpload fileUploader) {
        this.req = req;
        this.res = res;
        this.servletContext = servletContext;
        this.fileUploader = fileUploader;
        httpMethod = ObjectUtil.getValue(req.getMethod(), HttpMethod.class, null);

        init();
    }

    protected void init() {
    }


    /*--------- Auth Methods ---------*/

    public AuthToken<?> getAuthToken() {
        return authToken;
    }

    public void setAuthToken(AuthToken<?> auth) {
        this.authToken = auth;
    }

    @SuppressWarnings("unchecked")
    public <T> T getUser(Class<T> userClass) {
        if (authToken != null && userClass.isInstance(authToken.getUser())) {
            return (T) authToken.getUser();
        } else {
            return null;
        }
    }

    /*--------- /Auth Methods ---------*/

    // --------- Attribute Access --------- //
    public void setAttribute(String name, Object obj) {
        req.setAttribute(name, obj);
    }

    public void removeAttribute(String name) {
        req.removeAttribute(name);
    }

    public Object getAttribute(String name) {
        return req.getAttribute(name);
    }

    public <T> T getAttributeAs(String name, Class<T> cls) {
        return getAttributeAs(name, cls, null);
    }

    public <T> T getAttributeAs(String name, Class<T> cls, T defaultValue) {
        Object value = req.getAttribute(name);
        if (value != null) {
            if (cls.isInstance(value)) {
                return (T) value;

            } else {
                // otherwise, get get the toString and try to get it with the ObjectUtil.
                return ObjectUtil.getValue(value.toString(), cls, defaultValue);
            }
        } else {
            return defaultValue;
        }
    }

    // --------- /Attribute Access --------- //

    // --------- restContentType --------- //
    public String getRestContentType() {
        return restContentType;
    }

    public void setRestContentType(String restContentType) {
        this.restContentType = restContentType;
    }

    // --------- restContentType --------- //

    // 
    public HttpMethod getMethod() {
        return httpMethod;
    }
    
    void setWebRequestType(WebRequestType webRequestType){
        this.webRequestType = webRequestType;
    }
    
    public WebRequestType getWebRequestType(){
        return webRequestType;
    }

    // --------- Path Var --------- //
    public <T> T getPathVarAs(String name, Class<T> cls) {
        return ObjectUtil.getValue(getPathVar(name), cls, null);
    }

    public <T> T getPathVarAs(String name, Class<T> cls, T defaultValue) {
        return ObjectUtil.getValue(getPathVar(name), cls, defaultValue);
    }

    public String getPathVar(String var) {
        if (pathVarMap != null) {
            return pathVarMap.get(var);
        } else {
            return null;
        }
    }

    // TODO: should not be public
    void setPathVarMap(Map<String, String> map) {
        pathVarMap = map;
    }

    // --------- /Path Var --------- //

    // --------- Param Methods --------- //

    // protected mostly for Mock objects
    protected void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
        isParamInitialized = true;
    }

    public Map<String, Object> getParamMap() {
        initParamsIfNeeded();
        return paramMap;
    }

    /**
     * A map of all the param starting with the prefix.
     * 
     * @param prefix
     * @return The key is the string after the prefix, and the value, is the value itself. Return null if no param
     *         started with the prefix.
     */
    public Map<String, Object> getParamMap(String prefix) {
        Map<String, Object> allParamMap = getParamMap();
        Map<String, Object> prefixParamMap = null;
        if (allParamMap != null && prefix != null) {
            for (String paramName : allParamMap.keySet()) {

                if (paramName.startsWith(prefix)) {
                    if (prefixParamMap == null) {
                        prefixParamMap = new HashMap<String, Object>();
                    }
                    Object value = allParamMap.get(paramName);
                    prefixParamMap.put(paramName.substring(prefix.length()), value);
                }
            }

        }
        return prefixParamMap;
    }

    /**
     * @param <T>
     * @param prefix
     * @param cls
     * @return The list of values given a param prefix
     */
    public <T> List<T> getParamMapValues(String prefix, Class<T> cls) {
        List<T> list = new ArrayList<T>();
        Map<String, Object> prefixParamMap = getParamMap(prefix);
        if (prefixParamMap != null) {
            for (Object valueObj : prefixParamMap.values()) {
                if (valueObj instanceof String) {
                    T value = ObjectUtil.getValue((String) valueObj, cls, null);
                    if (value != null) {
                        list.add(value);
                    }
                }

            }
        }
        return list;
    }

    /**
     * Simple method to get the Request value for a given parameter name. Return null if the value is null or empty.
     * 
     * @param name
     *            parameter value
     * @return
     */
    public String getParam(String name) {
        return getParamAs(name, String.class, null);
    }

    /**
     * Return the param value as "cls" class object. If the value is null (or empty string) return null.
     * 
     * @param <T>
     * @param name
     * @param cls
     * @return the http param value if exist or valid, otherwise, null.
     */
    public <T> T getParamAs(String name, Class<T> cls) {
        return getParamAs(name, cls, null);
    }

    /**
     * Return the param value as "cls" class object. If the value is null (or empty string) return the defaultValue.<br>
     * 
     * Note: For the first call, this method will parse the request (in case of a multipart).<br />
     * 
     * <div class="issues"> <strong>Issues</strong>
     * <ul>
     * <li>FIXME: Need to fix the multipart handling. Can be simplified</li>
     * </ul>
     * </div>
     * 
     * @param <T>
     *            Class of the return element
     * @param name
     *            of the parameter
     * @param cls
     *            Class of the return element
     * @param defaultValue
     *            Default value in case of an error or null/empty value
     * @return
     */
    public <T> T getParamAs(String name, Class<T> cls, T defaultValue) {
        Map<String, Object> paramMap = getParamMap();
        if (paramMap == null) {
            return defaultValue;
        }
        // if we have a primitive type or array, then, just get the single value and convert it to the appropriate type
        if (ObjectUtil.isPrimitive(cls) || cls.isArray() || cls == FileItem.class || cls.isEnum()) {
            // first, try to get it from the paramMap
            Object valueObject = paramMap.get(name);

            if (isMultipart) {
                // HACK
                // if not found, try to get it from the regular HttpServletRequest
                // (in the case of a multiPart post,
                // HttpServletRequest.getParameter still have the URL params)
                if (valueObject == null) {
                    valueObject = getReq().getParameter(name);
                }
            }

            if (valueObject == null) {
                return defaultValue;
            } else if (valueObject instanceof String) {
                return (T) ObjectUtil.getValue((String) valueObject, cls, defaultValue);
            } else if (valueObject instanceof String[]) {
                return (T) ObjectUtil.getValue((String[]) valueObject, cls, defaultValue);
            } else {
                // hope for the best (should be a fileItem)
                return (T) valueObject;
            }
        }
        // otherwise, if it is not a primitive type, attempt to create the targeted object with the corresponding
        // paramMap
        else {
            Map subParamMap = getParamMap(name + "."); // i.e., "product."
            if (subParamMap != null) {
                try {
                    T value = cls.newInstance();
                    ObjectUtil.populate(value, subParamMap);
                    return value;
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                    return defaultValue;
                }
            } else {
                return defaultValue;
            }

        }
    }

    private void initParamsIfNeeded() {
        if (!isParamInitialized) {
            isMultipart = ServletFileUpload.isMultipartContent(getReq());
            if (isMultipart) {
                try {
                    fileItems = fileUploader.parseRequest(getReq());
                    paramMap = new HashMap<String, Object>();

                    Map<String, Class> paramBaseClasses = new HashMap<String, Class>();
                    boolean hasMultivalues = false;

                    for (Object item : fileItems) {
                        FileItem fileItem = (FileItem) item;
                        String paramName = fileItem.getFieldName();

                        // in case of normal fields, take the string value. otherwise
                        // put the whole file item into the map.
                        Object value;
                        Class paramBaseClass;
                        if (fileItem.isFormField()) {
                            try {
                                value = fileItem.getString("UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                value = fileItem.getString();
                            }
                            paramBaseClass = String.class;
                        } else {
                            value = fileItem;
                            paramBaseClass = FileItem.class;
                        }

                        // make sure that the client isn't calling something that is mixing and
                        // matching parameter types...
                        // todo - could support this as Object arrays or arrays of most specific shared super class.
                        Class prevBaseClass = paramBaseClasses.put(paramName, paramBaseClass);
                        if (prevBaseClass != null && !prevBaseClass.equals(paramBaseClass)) {
                            throw new IllegalArgumentException("parameter " + paramName
                                                    + " has mixed parameter types (expected all file or all string)");
                        }

                        // if there is already a value, then, create a list
                        if (paramMap.containsKey(paramName)) {
                            hasMultivalues = true;
                            Object prevValue = paramMap.get(paramName);
                            if (prevValue instanceof List) {
                                ((List) prevValue).add(value);
                            } else {
                                List values = new ArrayList(2);
                                values.add(prevValue);
                                values.add(value);
                                paramMap.put(paramName, values);
                            }
                        } else {
                            paramMap.put(paramName, value);
                        }
                    }

                    // if we had multivalues, need to change the list to arrays
                    if (hasMultivalues) {
                        for (String name : paramMap.keySet()) {
                            Object value = paramMap.get(name);
                            if (value instanceof List) {
                                List valueList = (List) value;
                                Object[] valueArray = (Object[]) Array.newInstance(paramBaseClasses.get(name), valueList.size());
                                valueList.toArray(valueArray);
                                paramMap.put(name, valueArray);
                            }
                        }
                    }
                } catch (FileUploadException e) {
                    // TODO Auto-generated catch block
                    logger.error(e.getMessage());
                }
            } else {
                paramMap = new HashMap<String, Object>();
                // By the httpServletRequest spect, we can assume the type of
                // the return Map (name and values)
                Map<String, String[]> reqMap = getReq().getParameterMap();
                // now, simplify the map, by replacing single string array to
                // the string itself.
                for (String paramName : reqMap.keySet()) {
                    String[] values = reqMap.get(paramName);
                    if (values.length == 1) {
                        paramMap.put(paramName, values[0]);
                    } else {
                        paramMap.put(paramName, values);
                    }
                }
            }
            isParamInitialized = true;
        }
    }

    // --------- /Param Methods --------- //

	// --------- Data Methods --------- //

	/**
	 * Get a singleton data by its object type
	 * @param cls
	 * @param <T>
	 * @return
	 */
	public <T> T getData(Class<T> cls){
		return (T) singletonDataMap.get(cls);
	}

	/**
	 * Set a singleton data (i.e. single instance of this type)
	 * @param obj
	 * @return
	 */
	public RequestContext setData(Object obj){
		if (obj != null){
			singletonDataMap.put(obj.getClass(),obj);
		}
		return this;
	}

	/**
	 * Remove a singleton data.
	 * @param cls
	 * @return
	 */
	public RequestContext removeData(Class cls){
		singletonDataMap.remove(cls);
		return this;
	}

	/**
	 * Get a named data.
	 * @param name
	 * @return
	 */
	public Object getData(String name){
		Object data = null;
		if (name != null) {
			data = namedDataMap.get(name);
		}
		return data;
	}

	/**
	 * Set a named data.
	 * @param name
	 * @param data
	 * @return
	 */
	public RequestContext setData(String name, Object data){
		if (name != null){
			namedDataMap.put(name,data);
		}
		return this;
	}

	/**
	 * Remove a named data
	 * @param name
	 * @return
	 */
	public RequestContext removeData(String name){
		if (name != null) {
			namedDataMap.remove(name);
		}
		return this;
	}
	// --------- /Data Methods --------- //

    /*--------- Cookie Methods ---------*/
	// For the RequestContextMock (allow to easily set cookies for test APIs)
	protected void setCookieMap(Map cookieMap){
		this.cookieMap = cookieMap;
	}

    public Map<String, String> getCookieMap() {
        if (cookieMap == null) {
            cookieMap = new HashMap<String, String>();
            Cookie[] cookies = getReq().getCookies();

            if (cookies != null) {
                for (Cookie c : cookies) {
                    String value = c.getValue();
                    try {
                        value = URLDecoder.decode(value, "UTF-8");
                    } catch (Exception e) {
                        // YES, ignore for now. If failed, the raw value will be in the cookie.
                    }
                    cookieMap.put(c.getName(), value);
                }
            }
        }
        return cookieMap;
    }

    public String getCookie(String name) {
        return getCookieMap().get(name);
    }

    public <T> T getCookie(String name, Class<T> cls, T defaultValue) {
        return ObjectUtil.getValue(getCookie(name), cls, defaultValue);
    }

    /**
     * set a cookie for a session lifecycle (this is the same as calling setCookie(name,value, false)
     * 
     * @param name
     * @param value
     *            if null, cookie for this name will be removed.
     */
    public void setCookie(String name, Object value) {
        setCookie(name, value, false);
    }

    /**
     * Set a cookie value.
     * 
     * @param name
     * @param value
     *            if null, will remove the cookie.
     * @param persistent
     *            if persistent, the cookie is set to persist for a very long time (one year)
     */
    public void setCookie(String name, Object value, boolean persistent) {
        // update the response
        HttpRequestUtil.setCookieValue(getRes(), name, value, persistent);
        // update the cookieMap (making sure that the template gets what we send to the browser)
        if (value != null) {
            getCookieMap().put(name, value.toString());
        } else {
            removeCookie(name);
        }
    }

    public void removeCookie(String name) {
        // update the response
        HttpRequestUtil.removeCookie(getReq(), getRes(), name);
        // update the cookieMap
        getCookieMap().remove(name);
    }

    /*--------- /Cookie Methods ---------*/

    // --------- Paths --------- //
    public String[] getResourcePaths() {
        return splitPath(getResourcePath());
    }

    public String getResourcePathAt(int i) {
        return pathAt(getResourcePaths(), i);
    }

    public <T> T getResourcePathAt(int i, Class<T> cls) {
        return pathAt(getResourcePaths(), i, cls, null);
    }

    public <T> T getResourcePathAt(int i, Class<T> cls, T defaultValue) {
        return pathAt(getResourcePaths(), i, cls, defaultValue);
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public void setFramePaths(String[] framePaths) {
        if (framePaths != null) {
            ArrayDeque<String> stack = new ArrayDeque<String>();
            stack.addAll(Arrays.asList(framePaths));
            framePathsStack = stack;
        }
    }

    public String popFramePath() {
        if (framePathsStack != null && !framePathsStack.isEmpty()) {
            return framePathsStack.pop();
        } else {
            return null;
        }
    }

    // --------- /Paths --------- //

    // --------- Utilities for Paths --------- //
    // TODO: probably does not need to be that complicated. Might want to use Guava here.
    static public String[] splitPath(String path) {
        String[] paths = null;
        String[] tmpPaths = path.split("/");
        // remove the first element (always empty since the currentPri
        // start starts with "/")
        if (tmpPaths.length > 1) {
            paths = new String[tmpPaths.length - 1];
            if (tmpPaths.length > 1) {
                System.arraycopy(tmpPaths, 1, paths, 0, paths.length);
            }
        } else {
            paths = new String[0];
        }
        return paths;
    }

    static private String pathAt(String[] paths, int i) {
        if (paths.length > i) {
            return paths[i];
        } else {
            return null;
        }
    }

    public <T> T pathAt(String[] paths, int i, Class<T> cls, T defaultValue) {
        String valueStr = pathAt(paths, i);
        return ObjectUtil.getValue(valueStr, cls, defaultValue);
    }

    // --------- /Utilities for Paths --------- //

    /*--------- Writer ---------*/
    public Writer getWriter() {
        try {
            return res.getWriter();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    /*--------- /Writer ---------*/

    // --------- Result --------- //
    public void setResult(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    // --------- /Result --------- //

    /*--------- RootModel ---------*/
    /**
     * Return the value in the model map (the m.**) with the appropriate type or null if not found.
     * 
     * @see #getModelValue(String, Class, Object)
     */
    public <T> T getModelValue(String namePath, Class<T> cls) {
        return getModelValue(namePath, cls, null);
    }

    /**
     * Return the value in the model map (the m.**) with the appropriate type and fall back value.
     * 
     * @param <T>
     * @param namePath
     *            path deliminated with the ".". Note that the "m." should not be in this namePath.
     * @param cls
     *            The type of the value to be casted to
     * @param defaultValue
     *            The fall back value
     * @return
     */
    public <T> T getModelValue(String namePath, Class<T> cls, T defaultValue) {
        return MapUtil.getDeepValue(webModel, namePath, cls, defaultValue);
    }

    /**
     * @return the Model M (use for the page model "m")
     */
    public Map getWebModel() {
        return webModel;
    }

    /*--------- /RootModel ---------*/

    public WebActionResponse getWebActionResponse() {
        return webActionResponse;
    }

    public void setWebActionResponse(WebActionResponse webActionResponse) {
        this.webActionResponse = webActionResponse;
    }

    /*--------- HttpServlet ---------*/
    public HttpServletRequest getReq() {
        return req;
    }

    public HttpServletResponse getRes() {
        return res;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public String getContextPath() {
        HttpServletRequest request = getReq();
        if (request != null) {
            return request.getContextPath();
        }
        return null;
    }

    public String getPathInfo() {
        if (pathInfo == null) {
            if (req != null) {
                // first try the traditional way
                pathInfo = req.getPathInfo();

                // otherwise build it from the requestURI
                if (pathInfo == null) {
                    // remove the contextPath
                    pathInfo = req.getRequestURI().substring(req.getContextPath().length());

                    try {
                        pathInfo = URLDecoder.decode(pathInfo, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        logger.error(e.getMessage());
                    }
                }
            }
        }
        return pathInfo;
    }

    /*--------- /HttpServlet ---------*/

    // --------- Abort Exception --------- //
    public AbortException getAbortException() {
        return abortException;
    }

    public void setAbortException(AbortException abortException) {
        this.abortException = abortException;
    }
    // --------- /Abort Exception --------- //
}

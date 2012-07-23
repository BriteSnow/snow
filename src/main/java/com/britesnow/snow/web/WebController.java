package com.britesnow.snow.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.britesnow.snow.util.FileUtil;
import com.britesnow.snow.util.MapUtil;
import com.britesnow.snow.util.Pair;
import com.britesnow.snow.web.AbortWithHttpStatusException.HttpStatus;
import com.britesnow.snow.web.auth.AuthToken;
import com.britesnow.snow.web.auth.AuthRequest;
import com.britesnow.snow.web.db.hibernate.HibernateSessionInViewHandler;
import com.britesnow.snow.web.handler.WebHandlerContext;
import com.britesnow.snow.web.handler.WebHandlerException;
import com.britesnow.snow.web.less.LessProcessor;
import com.britesnow.snow.web.path.FramePathsResolver;
import com.britesnow.snow.web.path.ResourceFileResolver;
import com.britesnow.snow.web.path.ResourcePathResolver;
import com.britesnow.snow.web.renderer.WebBundleManager;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class WebController {
    static private Logger                   logger                        = LoggerFactory.getLogger(WebController.class);

    private static final String             CHAR_ENCODING                 = "UTF-8";
    
    public static int                       BUFFER_SIZE                   = 2048 * 2;

    private ServletFileUpload               fileUploader;

    // For now, a very simple cache for the .less.css result
    private Map<String, Pair<Long, String>> lessCache                     = new ConcurrentHashMap<String, Pair<Long, String>>();

    @Inject
    private HttpWriter                      httpWriter;

    @Inject(optional = true)
    private ServletContext                  servletContext;
    @Inject
    private Application                     application;
    @Inject
    private WebBundleManager                webBundleManager;

    @Inject(optional = true)
    private AuthRequest                     authService;

    @Inject(optional = true)
    private HibernateSessionInViewHandler   hibernateSessionInViewHandler = null;

    @Inject(optional = true)
    private RequestLifecycle                requestLifeCycle              = null;

    @Inject
    private FramePathsResolver              framePathsResolver;

    @Inject
    private ResourcePathResolver            resourcePathResolver;

    @Inject
    private ResourceFileResolver                pathFileResolver;

    private ThreadLocal<RequestContext>     requestContextTl              = new ThreadLocal<RequestContext>();

    private CurrentRequestContextHolder     currentRequestContextHolder   = new CurrentRequestContextHolder() {
                                                                              @Override
                                                                              public RequestContext getCurrentRequestContext() {

                                                                                  return requestContextTl.get();
                                                                              }
                                                                          };

    @Inject
    private LessProcessor                   lessProcessor;

    // will be injected from .properties file
    // FIXME: need to implement this.
    // private boolean ignoreTemplateNotFound = false;

    public CurrentRequestContextHolder getCurrentRequestContextHolder() {
        return currentRequestContextHolder;
    }

    // --------- Injects --------- //
    @Inject(optional = true)
    public void injectIgnoreTemplateNotFound(@Named("snow.ignoreTemplateNotFound") String ignore) {
        if ("true".equalsIgnoreCase(ignore)) {
            // ignoreTemplateNotFound = true;
        }
    }

    public void init() {
        application.init();

        /* --------- Initialize the FileUploader --------- */
        // Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // Set factory constraints
        // factory.setSizeThreshold(yourMaxMemorySize);
        // factory.setRepository(yourTempDirectory);

        fileUploader = new ServletFileUpload(factory);
        /* --------- /Initialize the FileUploader --------- */
    }

    public void destroy() {
        application.shutdown();
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding(CHAR_ENCODING);
        response.setCharacterEncoding(CHAR_ENCODING);

        RequestContext rc = new RequestContext(request, response, servletContext, fileUploader);
        service(rc);
    }

    public void service(RequestContext rc) {
        ResponseType responseType = null;
        try {
            requestContextTl.set(rc);

            HttpServletRequest request = rc.getReq();

            // get the request resourcePath
            String resourcePath = resourcePathResolver.resolve(rc.getPathInfo(),rc);

            // --------- Resolve the ResponseType --------- //
            // determine the requestType
            if (application.hasWebResourceHandlerFor(resourcePath)) {
                responseType = ResponseType.webResource;
            } else if (isTemplatePath(resourcePath)) {
                responseType = ResponseType.template;
            } else if (isWebActionResponseJson(resourcePath,rc)) {
                responseType = ResponseType.webActionResponseJson;
            } else if (isJsonPath(resourcePath)) {
                responseType = ResponseType.json;
            } else if (webBundleManager.isWebBundle(resourcePath)) {
                responseType = ResponseType.webBundle;
            } else if (resourcePath.endsWith(".less.css")) {
                responseType = ResponseType.lessCss;
            } else {
                responseType = ResponseType.file;
            }
            // --------- /Resolve the ResponseType --------- //

            // set the resourcePath and fix it if needed
            switch (responseType) {
                case json:
                case template:
                    rc.setResourcePath(fixTemplateAndJsonResourcePath(resourcePath));
                    break;
                default:
                    rc.setResourcePath(resourcePath);
                    break;
            }

            // if we have template request, then, resolve the framePaths
            if (responseType == ResponseType.template) {
                String[] framePaths = framePathsResolver.resolve(rc);
                rc.setFramePaths(framePaths);
            }

            // --------- Open HibernateSession --------- //
            if (hibernateSessionInViewHandler != null) {
                hibernateSessionInViewHandler.openSessionInView();
            }
            // --------- /Open HibernateSession --------- //

            // --------- Auth --------- //
            if (authService != null) {
                AuthToken<?> auth = authService.authRequest(rc);
                rc.setAuthToken(auth);
            }
            // --------- /Auth --------- //

            // --------- RequestLifeCycle Start --------- //
            if (requestLifeCycle != null) {
                requestLifeCycle.start(rc);
            }
            // --------- /RequestLifeCycle Start --------- //

            // --------- Processing the Post (if any) --------- //
            if ("POST".equals(request.getMethod())) {
                String actionName = resolveWebActionName(rc);
                if (actionName != null) {
                    WebActionResponse webActionResponse = null;
                    try {
                        webActionResponse = application.processWebAction(actionName, rc);

                    } catch (Throwable e) {
                        if (e instanceof InvocationTargetException) {
                            e = e.getCause();
                        }
                        // TODO Need to handle exception
                        logger.error(getLogErrorString(e));
                        webActionResponse = new WebActionResponse(e);
                    }
                    rc.setWebActionResponse(webActionResponse);
                }

                // --------- afterActionProcessing --------- //
                if (hibernateSessionInViewHandler != null) {
                    hibernateSessionInViewHandler.afterActionProcessing();
                }
                // --------- /afterActionProcessing --------- //
            }
            // --------- /Processing the Post (if any) --------- //

            serviceRequestContext(responseType, rc);

            // this catch is for when this exception is thrown prior to entering the web handler method.
            // (e.g. a WebHandlerMethodInterceptor).
        } catch (Throwable t) {
            try {
                t = findEventualInvocationTargetException(t);

                WebHandlerContext webHandlerContext = null;
                if (t instanceof WebHandlerException) {
                    t = ((WebHandlerException) t).getCause();
                    webHandlerContext = ((WebHandlerException) t).getWebHandlerContext();
                }
                // first, try to see if the application process it with its WebExceptionHandler;
                boolean exceptionProcessed = application.processWebExceptionCatcher(t, webHandlerContext, rc);

                if (!exceptionProcessed) {
                    serviceRequestContext(responseType, rc);
                }

            } catch (AbortWithHttpStatusException e) {
                sendHttpError(rc, e.getStatus(), e.getMessage());
            } catch (AbortWithHttpRedirectException e) {
                sendHttpRedirect(rc, e);
            } catch (Throwable e) {
                // and now we have to double-handle this one b/c it will be propagated as an InvocationTargetException
                // when it's thrown from within a web handler.
                if (e instanceof AbortWithHttpStatusException) {
                    sendHttpError(rc, ((AbortWithHttpStatusException) e).getStatus(), e.getMessage());
                } else if (e instanceof AbortWithHttpRedirectException) {
                    sendHttpRedirect(rc, (AbortWithHttpRedirectException) e);
                } else {
                    // and this is the normal case...
                    sendHttpError(rc, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                    logger.error(getLogErrorString(e));
                }
            }

        } finally {
            // --------- RequestLifeCycle End --------- //
            if (requestLifeCycle != null) {
                requestLifeCycle.end(rc);
            }
            // --------- /RequestLifeCycle End --------- //

            // Remove the requestContext from the threadLocal
            // NOTE: might want to do that after the closeSessionInView.
            requestContextTl.remove();

            // --------- Close HibernateSession --------- //
            if (hibernateSessionInViewHandler != null) {
                hibernateSessionInViewHandler.closeSessionInView();
            }
            // --------- /Close HibernateSession --------- //

        }

    }

    public void serviceRequestContext(ResponseType responseType, RequestContext rc) {
        switch (responseType) {
            case template:
                serviceTemplate(rc);
                break;
            case json:
                serviceJson(rc);
                break;
            case webActionResponseJson:
                serviceWebActionResponse(rc);
                break;
            case webResource:
                serviceWebResource(rc);
                break;
            case webBundle:
                serviceWebBundle(rc);
                break;
            case lessCss:
                serviceLessCss(rc);
                break;
            case file:
                serviceFile(rc);
                break;
        }
    }

    // --------- Service Request --------- //
    private void serviceTemplate(RequestContext rc) {
        HttpServletRequest req = rc.getReq();
        HttpServletResponse res = rc.getRes();

        try {
            
            // TODO: probably need to remove this, not sure it does anything here (or it even should be here.
            req.setCharacterEncoding(CHAR_ENCODING);
            

            // TODO: needs to implement this
            /*
             * if (!ignoreTemplateNotFound && !webApplication.getPart(part.getPri()).getResourceFile().exists()) {
             * sendHttpError(rc, HttpServletResponse.SC_NOT_FOUND, null); return; }
             */

            res.setContentType("text/html;charset=" + CHAR_ENCODING);
            // if not cachable, then, set the appropriate headers.
            res.setHeader("Pragma", "No-cache");
            res.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
            res.setDateHeader("Expires", 1);

            application.processTemplate(rc);

            rc.getWriter().close();
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }

    }

    private void serviceWebActionResponse(RequestContext rc) {
        setJsonHeaders(rc);

        application.processWebActionResponseJson(rc);

        try {
            rc.getWriter().close();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private void serviceJson(RequestContext rc) {
        setJsonHeaders(rc);
        application.processJson(rc);

        try {
            rc.getWriter().close();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private void setJsonHeaders(RequestContext rc) {
        HttpServletRequest req = rc.getReq();
        HttpServletResponse res = rc.getRes();

        // NOTE: we might want to let the render deal with this (not sure)
        try {
            req.setCharacterEncoding(CHAR_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }
        res.setContentType("application/json");

        // no cash for now.
        res.setHeader("Pragma", "No-cache");
        res.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
        res.setDateHeader("Expires", 1);
    }

    public void serviceWebBundle(RequestContext rc) {
        String contextPath = rc.getContextPath();
        String resourcePath = rc.getResourcePath();

        String href = new StringBuilder(contextPath).append(resourcePath).toString();
        String content = webBundleManager.getContent(resourcePath);
        StringReader reader = new StringReader(content);
        httpWriter.writeStringContent(rc, href, reader, true, null);
    }

    private void serviceWebResource(RequestContext rc) {
        application.processWebResourceHandler(rc);
    }

    private void serviceLessCss(RequestContext rc) {
        String resourcePath = rc.getResourcePath();

        // --------- Process the .less file --------- //
        String lessFilePath = resourcePath.substring(0, resourcePath.length() - 4);

        File lessFile = pathFileResolver.resolve(lessFilePath,rc);
        if (!lessFile.exists()) {
            throw new AbortWithHttpStatusException(HttpStatus.NOT_FOUND, "File " + lessFilePath + " not found");
        }

        // Now, determine the youngest .less file
        long maxTime = 0;
        File lessFolder = lessFile.getParentFile();
        for (File file : lessFolder.listFiles()) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".less")) {
                long time = file.lastModified();
                if (time > maxTime) {
                    maxTime = time;
                }
            }
        }

        String lessResult = null;
        Pair<Long, String> timeAndContent = lessCache.get(lessFilePath);
        if (timeAndContent != null && maxTime <= timeAndContent.getFirst()) {
            // if we have a match, and none of the maxTime of all the .less file still smaller or equal than the cache
            // item, then, still valid
            lessResult = timeAndContent.getSecond();
        } else {
            lessResult = lessProcessor.compile(lessFile);
            lessCache.put(lessFilePath, new Pair<Long, String>(maxTime, lessResult));
        }
        // --------- /Process the .less file --------- //

        Reader contentReader = new StringReader(lessResult);
        String fileName = FileUtil.getFilePathAndName(resourcePath)[1];
        httpWriter.writeStringContent(rc, fileName, contentReader, true, null);
    }

    private void serviceFile(RequestContext rc) {
        String resourcePath = rc.getResourcePath();
        File resourceFile = pathFileResolver.resolve(resourcePath,rc);
        if (resourceFile.exists()) {
            boolean isCachable = isCachable(resourcePath);
            httpWriter.writeFile(rc, resourceFile, isCachable, null);
        } else {
            sendHttpError(rc, HttpServletResponse.SC_NOT_FOUND, null);
        }
    }

    // --------- /Service Request --------- //
    private void sendHttpError(RequestContext rc, int errorCode, String message) {

        // if the response has already been committed, there's not much we can do about it at this point...just let it
        // go.
        // the one place where the response is likely to be committed already is if the exception causing the error
        // originates while processing a template. the template will usually have already output enough html so that
        // the container has already started writing back to the client.
        if (!rc.getRes().isCommitted()) {
            try {
                rc.getRes().sendError(errorCode, message);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    private void sendHttpRedirect(RequestContext rc, AbortWithHttpRedirectException e){

        // like above, there's not much we can do if the response has already been committed. in that case,
        // we'll just silently ignore the exception.
        HttpServletResponse response = rc.getRes();
        if (!response.isCommitted()) {
            response.setStatus(e.getRedirectCode());
            response.addHeader("Location", e.getLocation());
        }
    }

    /*
     * if it it and InvocationTargetException or RuntimeException that wrap a InvocationTargetException, then, return
     * the target.
     */
    static private Throwable findEventualInvocationTargetException(Throwable t) {
        InvocationTargetException ie = null;
        if (t instanceof InvocationTargetException) {
            ie = (InvocationTargetException) t;
        } else if (t.getCause() != null && t.getCause() instanceof InvocationTargetException) {
            ie = (InvocationTargetException) t.getCause();
        }
        if (ie != null) {
            t = ie.getTargetException();
        }

        return t;
    }

    /*
     * First the resourcePath by remove extension (.json or .ftl) and adding index if the path end with "/"
     */
    static private String fixTemplateAndJsonResourcePath(String resourcePath) {
        String path = FileUtil.getFileNameAndExtension(resourcePath)[0];
        return path;
    }

    static Set cachableExtension = MapUtil.setIt(".css", ".less", ".js", ".png", ".gif", ".jpeg");

    /**
     * Return true if the content pointed by the pathInfo is static.<br>
     * Right now, just return true if there is no extension
     * 
     * @param path
     * @return
     */
    static private final boolean isTemplatePath(String path) {

        if (path.lastIndexOf('.') == -1 || path.endsWith(".ftl")) {
            return true;
        } else {
            return false;
        }
    }

    static private final boolean isJsonPath(String path) {
        if (path.endsWith(".json")) {
            return true;
        } else {
            return false;
        }
    }
    
    static private final boolean isWebActionResponseJson(String resourcePath, RequestContext rc){
        if (rc.getReq().getMethod().equals("POST")){
            if (resourcePath.endsWith(".do") || "/_actionResponse.json".equals(resourcePath)){
                return true;
            }
        }
        return false;
    }
    
    static private final String resolveWebActionName(RequestContext rc){
        String resourcePath = rc.getResourcePath();
        
        // if it is a .do request
        if (resourcePath.endsWith(".do")){
            return resourcePath.substring(1,resourcePath.length() - 3);
        }
        
        return rc.getParam("action");
    }

    static final private boolean isCachable(String pathInfo) {
        String ext = FileUtil.getFileNameAndExtension(pathInfo)[1];
        return cachableExtension.contains(ext);
    }

    static final private String getLogErrorString(Throwable e) {
        StringBuilder errorSB = new StringBuilder();
        errorSB.append(e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        errorSB.append("\n-- StackTrace:\n").append(sw.toString()).append("\n-- /StackTrace");
        return errorSB.toString();
    }

}

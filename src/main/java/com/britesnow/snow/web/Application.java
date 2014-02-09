package com.britesnow.snow.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.britesnow.snow.SnowException;
import com.britesnow.snow.web.db.hibernate.HibernateSessionFactoryBuilder;
import com.britesnow.snow.web.exception.WebExceptionCatcherRef;
import com.britesnow.snow.web.exception.WebExceptionContext;
import com.britesnow.snow.web.handler.MethodInvoker;
import com.britesnow.snow.web.handler.WebActionHandlerRef;
import com.britesnow.snow.web.handler.WebHandlerContext;
import com.britesnow.snow.web.handler.WebObjectRegistry;
import com.britesnow.snow.web.handler.WebResourceHandlerRef;
import com.britesnow.snow.web.handler.WebModelHandlerRef;
import com.britesnow.snow.web.hook.AppPhase;
import com.britesnow.snow.web.hook.HookInvoker;
import com.britesnow.snow.web.hook.On;
import com.britesnow.snow.web.renderer.JsonRenderer;
import com.britesnow.snow.web.renderer.freemarker.FreemarkerTemplateRenderer;
import com.britesnow.snow.web.rest.RestRegistry;
import com.britesnow.snow.web.rest.SerializerRegistry;
import com.britesnow.snow.web.rest.WebRestRef;
import com.britesnow.snow.web.rest.WebSerializerRef;
import com.google.common.base.Throwables;
import com.google.inject.Inject;

@Singleton
public class Application {
    static private final Logger logger = LoggerFactory.getLogger(Application.class);

    public enum Error {
        NO_WEB_ACTION
    }

    private static final String            MODEL_KEY_REQUEST       = "_r";

    // just to make sure we initialize only once
    private volatile boolean               initialized             = false;

    @Inject(optional = true)
    private WebApplicationLifecycle        webApplicationLifeCycle = null;

    @Inject(optional = true)
    private HibernateSessionFactoryBuilder hibernateSessionFactoryBuilder;

    @Inject
    private FreemarkerTemplateRenderer     freemarkerRenderer;
    // private FreemarkerRenderer freemarkerRenderer;

    // TODO: need to use just the JsonRenderer when it will be an interface.
    @Inject
    private JsonRenderer                   jsonRenderer;

    @Inject
    private WebObjectRegistry              webObjectRegistry;

    @Inject
    private RestRegistry                   restRegistry;

    @Inject
    private SerializerRegistry             serializerRegistry;

    @Inject
    private HookInvoker                    hookInvoker;

    @Inject
    private MethodInvoker                  methodInvoker;

    // --------- LifeCycle --------- //
    public synchronized void init() {
        if (!initialized) {
            try {
                hookInvoker.invokeAppHooks(AppPhase.INIT, On.BEFORE);

                // initialize the hibernateSessionFactoryBuilder
                if (hibernateSessionFactoryBuilder != null && hibernateSessionFactoryBuilder instanceof Initializable) {
                    ((Initializable) hibernateSessionFactoryBuilder).init();
                }

                webObjectRegistry.init();

                // initialize freemarker
                freemarkerRenderer.init();

                // initialize the webApplicationLifeCycle if present
                if (webApplicationLifeCycle != null) {
                    webApplicationLifeCycle.init();
                }
                hookInvoker.invokeAppHooks(AppPhase.INIT, On.AFTER);
            } catch (Throwable e) {
                logger.error("Snow - Application init failed: " + e.getMessage(),e);
            }
            initialized = false;

        } else {
            logger.error("Application.init is being called more than one time. This should not happen, and it is ignored");
        }

    }

    public void shutdown() {

        hookInvoker.invokeAppHooks(AppPhase.SHUTDOWN, On.BEFORE);

        if (webApplicationLifeCycle != null) {
            webApplicationLifeCycle.shutdown();
        }

        hookInvoker.invokeAppHooks(AppPhase.SHUTDOWN, On.AFTER);
    }

    // --------- LifeCycle --------- //

    // --------- Content Processing --------- //

    public boolean hasAllowSubPathsWebModel(RequestContext rc){
        List<WebModelHandlerRef> refs = webObjectRegistry.getWebModelHandlerRefList(rc.getResourcePaths());
        boolean r = false;
        for (WebModelHandlerRef ref : refs){
            if (ref.getWebModelHandler().allowSubPaths()){
                r = true;
                break;
            }
        }
        return r;
    }
    
    public boolean hasResourcePathWebModel(RequestContext rc){
        WebModelHandlerRef ref = webObjectRegistry.getWebModeHandlerlRef(rc.getResourcePath());
        if (ref != null){
            return true;
        }else{
            return false;
        }
    }
    
    public boolean hasTemplate(RequestContext rc) {
        return freemarkerRenderer.hasTemplate(rc.getResourcePath(), rc);
    }

    public void processTemplate(RequestContext rc) throws Throwable {

        processWebModels(rc);

        Map templateModel = new HashMap();

        // Copy the model map and extend it with the _r request info
        Map model = rc.getWebModel();
        for (Object key : model.keySet()) {
            templateModel.put(key, model.get(key));
        }
        templateModel.put(MODEL_KEY_REQUEST, RequestInfoMapBuilder.buildRequestModel(rc));

        String path = rc.popFramePath();
        if (path == null) {
            path = rc.getResourcePath();
            path = getTemplatePath(path);
        }

        freemarkerRenderer.render(path, templateModel, rc.getWriter(), rc);
    }

    public void processWebActionResponseJson(RequestContext rc) {
        Object data = null;

        // quick hack for now.
        // Return raw result if no exception. otherwise, return WEbActionResponse
        WebActionResponse actionResponse = rc.getWebActionResponse();
        if (actionResponse != null && actionResponse.getError() == null) {
            data = actionResponse.getResult();
        } else if (actionResponse != null) {
            data = actionResponse;
        }
        if (data != null) {
            jsonRenderer.render(data, rc.getWriter());
        }
    }

    public void processJson(RequestContext rc) {
        Object data;

        processWebModels(rc);
        Map m = rc.getWebModel();

        // first try to get the _jsonData
        data = m.get("_jsonData");

        // if no _jsonData is not set, then, take the model as the data
        if (data == null) {
            data = m;
        }

        jsonRenderer.render(data, rc.getWriter());
    }

    public void processRest(RequestContext rc) {
        WebRestRef webRestRef = restRegistry.getWebRestRef(rc);

        try {
            setEventualPathVarMap(webRestRef,rc);
            Object result = methodInvoker.invokeWebRest(webRestRef, rc);
            rc.setResult(result);
            String contentType = rc.getRestContentType();
            WebSerializerRef ref = serializerRegistry.getWebSerializerRef(contentType);
            methodInvoker.invokeWebSerializer(ref, rc);
            // jsonRenderer.render(result, rc.getWriter());
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }
    
    private void setEventualPathVarMap(WebRestRef webRestRef, RequestContext rc){
        Pattern pattern = webRestRef.getPathPattern();
        
        if (pattern != null){
            Map<Integer,String> varNameById = webRestRef.getPathVarByIdx();
            Matcher uriMatcher =  pattern.matcher(rc.getResourcePath());
            
            Map<String,String> valueByParamName = new HashMap<String, String>();
            if (uriMatcher.matches()){
                for (int gi = 0; gi <= uriMatcher.groupCount(); gi++){
                    String value = uriMatcher.group(gi);
                    String pathParam = varNameById.get(gi - 1);
                    if (pathParam != null){
                        valueByParamName.put(pathParam, value);
                    }
                }
                rc.setPathVarMap(valueByParamName);
            }            
        }
    }

    // --------- /Content Processing --------- //

    // --------- WebHandler Processing --------- //
    WebActionResponse processWebAction(String actionName, RequestContext rc) {
        WebActionHandlerRef webActionRef = webObjectRegistry.getWebActionHandlerRef(actionName);

        if (webActionRef == null) {
            throw new SnowException(Error.NO_WEB_ACTION, "WebAction", actionName);
        }

        // --------- Invoke Method --------- //
        Object result = null;
        try {
            result = methodInvoker.invokeWebHandler(webActionRef, rc);
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
        // --------- /Invoke Method --------- //

        WebActionResponse response = new WebActionResponse(result);
        return response;
    }

    void processWebModels(RequestContext rc) {
        List<WebModelHandlerRef> refs = webObjectRegistry.getWebModelHandlerRefList(rc.getResourcePaths());

        for (WebModelHandlerRef ref : refs) {
            invokeWebModelRef(ref, rc);
        }

        // Match and process the "matches" webModels
        List<WebModelHandlerRef> matchWebModelRefs = webObjectRegistry.getMatchWebModelHandlerRef(rc.getResourcePath());
        for (WebModelHandlerRef webModelRef : matchWebModelRefs) {
            invokeWebModelRef(webModelRef, rc);
        }
    }

    private void invokeWebModelRef(WebModelHandlerRef webModelRef, RequestContext rc) {

        if (webModelRef != null) {
            try {
                methodInvoker.invokeWebHandler(webModelRef, rc);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    boolean hasWebResourceHandlerFor(String resourcePath) {
        return (webObjectRegistry.getWebResourceHandlerRef(resourcePath) != null);
    }

    void processWebResourceHandler(RequestContext rc) {
        WebResourceHandlerRef webResourceHandlerRef = webObjectRegistry.getWebResourceHandlerRef(rc.getResourcePath());
        if (webResourceHandlerRef != null) {
            methodInvoker.invokeWebHandler(webResourceHandlerRef, rc);
        } else {
            throw new RuntimeException("No WebResourceHandler for " + rc.getResourcePath());
        }
    }

    // --------- /WebHandler Processing --------- //

    // --------- WebExceptionCatcher Processing --------- //
    public boolean processWebExceptionCatcher(Throwable t, WebHandlerContext webHandlerContext, RequestContext rc) {
        WebExceptionContext webExceptionContext = new WebExceptionContext(webHandlerContext);

        WebExceptionCatcherRef webExceptionCatcherRef = webObjectRegistry.getWebExceptionCatcherRef(t.getClass());
        if (webExceptionCatcherRef != null) {
            methodInvoker.invokeWebException(webExceptionCatcherRef, t, webExceptionContext, rc);
            return true;
        } else {
            return false;
        }
    }

    // --------- /WebExceptionCatcher Processing --------- //

    String getTemplatePath(String resourcePath) {
        String[] leafPaths = webObjectRegistry.getLeafPaths();

        if (leafPaths == null || leafPaths.length < 1) {
            return resourcePath;
        } else {
            for (String leafTemplatePath : leafPaths) {
                if (resourcePath.startsWith(leafTemplatePath)) {
                    // remove the eventual '/'
                    if (leafTemplatePath.endsWith("/")) {
                        leafTemplatePath = leafTemplatePath.substring(0, leafTemplatePath.length() - 1);
                    }
                    return leafTemplatePath;
                }
            }
            // if the priWoExt does match any template, return the priWoExt
            return resourcePath;
        }
    }

}

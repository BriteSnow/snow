package com.britesnow.snow.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                hookInvoker.invokeAppHooks(AppPhase.INIT,On.BEFORE);
                
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
                hookInvoker.invokeAppHooks(AppPhase.INIT,On.AFTER);
            } catch (Throwable e) {
                logger.error("Snow - Application init failed: " + e.getMessage());
                e.printStackTrace();
            }
            initialized = false;

        } else {
            logger.error("Application.init is being called more than one time. This should not happen, and it is ignored");
        }

    }

    public void shutdown() {

        hookInvoker.invokeAppHooks(AppPhase.SHUTDOWN,On.BEFORE);

        if (webApplicationLifeCycle != null) {
            webApplicationLifeCycle.shutdown();
        }
        
        hookInvoker.invokeAppHooks(AppPhase.SHUTDOWN,On.AFTER);
    }

    // --------- LifeCycle --------- //

    // --------- Content Processing --------- //
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
    
    
    public void processRest(RequestContext rc){
        WebRestRef webRestRef = restRegistry.getWebRestRef(rc);
        
        try{
            Object result = methodInvoker.invokeWebRest(webRestRef,rc);
            rc.setResult(result);
            String contentType = rc.getRestContentType();
            WebSerializerRef ref = serializerRegistry.getWebSerializerRef(contentType);
            methodInvoker.invokeWebSerializer(ref, rc);
            //jsonRenderer.render(result, rc.getWriter());
        }catch (Throwable t) {
            throw Throwables.propagate(t);
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
        // get the rootModelBuilder
        WebModelHandlerRef rootWmr = webObjectRegistry.getWebModeHandlerlRef("/");
        if (rootWmr != null) {
            invokeWebModelRef(rootWmr, rc);
        }

        StringBuilder pathBuilder = new StringBuilder();
        String[] resourcePaths = rc.getResourcePaths();
        for (int i = 0; i < resourcePaths.length; i++) {
            String path = pathBuilder.append('/').append(resourcePaths[i]).toString();
            WebModelHandlerRef webModelRef = webObjectRegistry.getWebModeHandlerlRef(path);
            invokeWebModelRef(webModelRef, rc);
        }

        // Match and process the "matches" webModels
        List<WebModelHandlerRef> matchWebModelRefs = webObjectRegistry.getMatchWebModelHandlerRef(pathBuilder.toString());
        for (WebModelHandlerRef webModelRef : matchWebModelRefs) {
            invokeWebModelRef(webModelRef, rc);
        }
    }

    private void invokeWebModelRef(WebModelHandlerRef webModelRef, RequestContext rc) {

        if (webModelRef != null) {
            try {
                methodInvoker.invokeWebHandler(webModelRef,rc);
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
            methodInvoker.invokeWebHandler(webResourceHandlerRef,rc);
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
            methodInvoker.invokeWebException(webExceptionCatcherRef,t,webExceptionContext,rc);
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

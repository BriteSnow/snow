package com.britesnow.snow.web;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.britesnow.snow.SnowRuntimeException;
import com.britesnow.snow.web.binding.WebHandlers;
import com.britesnow.snow.web.db.hibernate.HibernateSessionFactoryBuilder;
import com.britesnow.snow.web.handler.WebActionHandlerRef;
import com.britesnow.snow.web.handler.WebHandlerInterceptor;
import com.britesnow.snow.web.handler.WebHandlerContext;
import com.britesnow.snow.web.handler.WebHandlerRegistry;
import com.britesnow.snow.web.handler.WebHandlerType;
import com.britesnow.snow.web.handler.WebResourceHandlerRef;
import com.britesnow.snow.web.handler.WebModelHandlerRef;
import com.britesnow.snow.web.renderer.JsonRenderer;
import com.britesnow.snow.web.renderer.freemarker.FreemarkerTemplateRenderer;
import com.google.common.base.Throwables;
import com.google.inject.Inject;

@Singleton
public class Application {
    static private final Logger logger = LoggerFactory.getLogger(Application.class);

    public enum Error {
        NO_WEB_ACTION
    }

    static final String                    PATH_ACTION_RESPONSE_JSON   = "/_actionResponse";

    // just to make sure we initialize only onces
    private boolean                        initialized                 = false;

    @Inject(optional = true)
    private WebHandlerInterceptor    webHandlerInterceptor = null;

    @Inject(optional = true)
    private WebApplicationLifeCycle        webApplicationLifeCycle     = null;

    @Inject(optional = true)
    private HibernateSessionFactoryBuilder hibernateSessionFactoryBuilder;

    @Inject
    private FreemarkerTemplateRenderer     freemarkerRenderer;
    // private FreemarkerRenderer freemarkerRenderer;

    // TODO: need to use just the JsonRenderer when it will be an interface.
    @Inject
    private JsonRenderer                   jsonRenderer;

    @Inject(optional = true)
    @Nullable
    @WebHandlers
    private Object[]                       webHandlers;

    @Inject
    private WebHandlerRegistry             webHandlerRegistry;

    // --------- LifeCycle --------- //
    public void init() {
        if (!initialized) {

            // initialize the hibernateSessionFactoryBuilder
            if (hibernateSessionFactoryBuilder != null && hibernateSessionFactoryBuilder instanceof Initializable) {
                ((Initializable) hibernateSessionFactoryBuilder).init();
            }

            webHandlerRegistry.init();
            // register the webhandlers
            if (webHandlers != null) {
                for (Object webHandler : webHandlers) {
                    webHandlerRegistry.registerWebHandlers(webHandler);
                }
            }

            // initialize freemarker
            freemarkerRenderer.init();

            // initialize the webApplicationLifeCycle if present
            if (webApplicationLifeCycle != null) {
                webApplicationLifeCycle.init();
            }

            initialized = false;

        } else {
            logger.error("Application.init is being called more than one time. This should not happen, and it is ignored");
        }

    }

    public void shutdown() {
        if (webApplicationLifeCycle != null) {
            webApplicationLifeCycle.shutdown();
        }
    }

    // --------- LifeCycle --------- //

    // --------- Content Processing --------- //
    public void processTemplate(RequestContext rc) throws Throwable {
        // build the new model
        Map rootModel = rc.getRootModel();

        processWebModels(rc);

        String path = rc.popFramePath();
        if (path == null) {
            path = rc.getResourcePath();
            path = getTemplatePath(path);
        }

        freemarkerRenderer.render(path, rootModel, rc.getWriter());
    }

    public void processJson(RequestContext rc) throws Throwable {
        String resourcePath = rc.getResourcePath();

        Object data;

        // if the resourcePath is the actionResponse path, then the data is the WebActionResponse
        if (resourcePath.equals(PATH_ACTION_RESPONSE_JSON)) {
            // quick hack for now. 
            // Return raw result if no exception. otherwise, return WEbActionResponse
            WebActionResponse actionResponse = rc.getWebActionResponse();
            if (actionResponse.getError() == null){
                data = actionResponse.getResult();
            }else{
                data = actionResponse;
            }
            
        }
        // otherwise, we process the webmodels
        else {
            processWebModels(rc);

            Map m = rc.getWebModel();

            // first try to get the _jsonData
            data = m.get("_jsonData");

            // if no _jsonData is not set, then, take the model as the data
            if (data == null) {
                data = m;
            }
        }
        jsonRenderer.render(data, rc.getWriter());
    }

    // --------- /Content Processing --------- //

    // --------- WebHandler Processing --------- //
    WebActionResponse processWebAction(String actionName, RequestContext rc) {
        WebActionHandlerRef webActionRef = webHandlerRegistry.getWebActionHandlerRef(actionName);

        if (webActionRef == null) {
            throw new SnowRuntimeException(Error.NO_WEB_ACTION, "WebAction", actionName);
        }

        // --------- Invoke Method --------- //
        boolean invokeWebAction = true;

        Object result = null;

        try {
            WebHandlerContext handlerContext = new WebHandlerContext(WebHandlerType.action,webActionRef.getHandlerObject(),webActionRef.getHandlerMethod());
            if (webHandlerInterceptor != null) {
                invokeWebAction = webHandlerInterceptor.beforeWebHandler(handlerContext,rc);
            }

            if (invokeWebAction) {
                result = webActionRef.invoke(rc);
            }

            if (invokeWebAction && webHandlerInterceptor != null) {
                webHandlerInterceptor.afterWebHandler(handlerContext, rc);
            }
        } catch (Throwable t) {
            // TODO: add support for WebExceptionCatcher
            throw Throwables.propagate(t);
        }
        // --------- /Invoke Method --------- //

        WebActionResponse response = new WebActionResponse(result);
        return response;
    }

    void processWebModels(RequestContext rc) throws Throwable {
        // get the rootModelBuilder
        WebModelHandlerRef rootWmr = webHandlerRegistry.getWebModeHandlerlRef("/");
        if (rootWmr != null) {
            invokeWebModelRef(rootWmr, rc);
        }

        StringBuilder pathBuilder = new StringBuilder();
        String[] resourcePaths = rc.getResourcePaths();
        for (int i = 0; i < resourcePaths.length; i++) {
            String path = pathBuilder.append('/').append(resourcePaths[i]).toString();
            WebModelHandlerRef webModelRef = webHandlerRegistry.getWebModeHandlerlRef(path);
            invokeWebModelRef(webModelRef, rc);
        }

        // Match and process the "matches" webModels
        List<WebModelHandlerRef> matchWebModelRefs = webHandlerRegistry.getMatchWebModelHandlerRef(pathBuilder.toString());
        for (WebModelHandlerRef webModelRef : matchWebModelRefs) {
            invokeWebModelRef(webModelRef, rc);
        }
    }

    private void invokeWebModelRef(WebModelHandlerRef webModelRef, RequestContext rc) {

        if (webModelRef != null) {

            boolean invokeWebModel = true;

            try {
                WebHandlerContext handlerContext = new WebHandlerContext(WebHandlerType.model,webModelRef.getHandlerObject(),webModelRef.getHandlerMethod());
                
                if (webHandlerInterceptor != null) {
                   invokeWebModel = webHandlerInterceptor.beforeWebHandler(handlerContext, rc);
                }

                if (invokeWebModel) {
                    webModelRef.invoke(rc);
                }

                if (invokeWebModel && webHandlerInterceptor != null) {
                    webHandlerInterceptor.afterWebHandler(handlerContext, rc);
                }
            } catch (Exception e) {
                // TODO: needs to add the support for WebExceptionCatcher
                throw Throwables.propagate(e);
            }

        }
    }

    boolean processWebResourceHandler(RequestContext rc) {
        WebResourceHandlerRef webResourceHandlerRef = webHandlerRegistry.getWebResourceHandlerRef(rc.getResourcePath());
        if (webResourceHandlerRef != null) {
            WebHandlerContext handlerContext = new WebHandlerContext(WebHandlerType.resource,webResourceHandlerRef.getHandlerObject(),webResourceHandlerRef.getHandlerMethod());
            
            boolean invokeWebResource = true;
            
            if (webHandlerInterceptor != null) {
                invokeWebResource = webHandlerInterceptor.beforeWebHandler(handlerContext, rc);
            }
            
            if (invokeWebResource){
                webResourceHandlerRef.invoke(rc);
            }
            
            if (invokeWebResource && webHandlerInterceptor != null) {
                webHandlerInterceptor.afterWebHandler(handlerContext, rc);
            }            
            
            return true;
        } else {
            return false;
        }
    }

    // --------- /WebHandler Processing --------- //

    public String getTemplatePath(String resourcePath) {
        String[] leafPaths = webHandlerRegistry.getLeafPaths();

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

package com.britesnow.snow.web;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import com.britesnow.snow.web.handler.WebExceptionHandlerRef;
import com.britesnow.snow.web.handler.WebResourceHandlerRef;
import com.britesnow.snow.web.handler.WebModelHandlerRef;
import com.britesnow.snow.web.handler.WebTemplateDirectiveHandlerRef;
import com.britesnow.snow.web.handler.annotation.WebActionHandler;
import com.britesnow.snow.web.handler.annotation.WebExceptionHandler;
import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.handler.annotation.WebResourceHandler;
import com.britesnow.snow.web.handler.annotation.WebTemplateDirectiveHandler;
import com.britesnow.snow.web.param.WebParameterParser;
import com.britesnow.snow.web.renderer.JsonRenderer;
import com.britesnow.snow.web.renderer.freemarker.FreemarkerTemplateRenderer;
import com.britesnow.snow.web.renderer.freemarker.TemplateDirectiveProxy;
import com.google.inject.Inject;

@Singleton
public class Application {
    static private final Logger                                     logger                     = LoggerFactory.getLogger(Application.class);
    
    public enum Error{
        NO_WEB_ACTION
    }
    
    static final String                                             PATH_ACTION_RESPONSE_JSON  = "/_actionResponse";

    // just to make sure we initialize only onces
    private boolean                                                 initialized                = false;

    @Inject(optional = true)
    private WebHandlerInterceptor                                   webHandlerInterceptor      = null;

    @Inject(optional = true)
    private WebApplicationLifeCycle                                      webApplicationLifeCycle    = null;

    @Inject(optional = true)
    private HibernateSessionFactoryBuilder                          hibernateSessionFactoryBuilder;

    @Inject(optional = true)
    private WebParameterParser[]                                    webParameterParsers;
    

    @Inject
    private FreemarkerTemplateRenderer                              freemarkerRenderer;
    // private FreemarkerRenderer freemarkerRenderer;

    // TODO: need to use just the JsonRenderer when it will be an interface.
    @Inject
    private JsonRenderer                                     jsonRenderer;

    @Inject(optional = true)
    @Nullable
    @WebHandlers
    private Object[]                                                webHandlers;

    private String[] leafPaths;
    
    // --------- Populated During Initialization --------- //
    private Map<String, WebModelHandlerRef>                         webModelByStartsWithMap    = new HashMap<String, WebModelHandlerRef>();
    private List<WebModelHandlerRef>                                webModelRefList            = new ArrayList<WebModelHandlerRef>();
    private Map<String, WebActionHandlerRef>                        webActionDic               = new HashMap<String, WebActionHandlerRef>();
    private List<WebResourceHandlerRef>                                 webFileList                = new ArrayList<WebResourceHandlerRef>();
    private Map<Class<? extends Throwable>, WebExceptionHandlerRef> webExceptionHanderMap      = new HashMap<Class<? extends Throwable>, WebExceptionHandlerRef>();
    private Map<Class<? extends Annotation>, WebParameterParser>    webParameterParserMap      = new HashMap<Class<? extends Annotation>, WebParameterParser>();
    private List<TemplateDirectiveProxy>                            templateDirectiveProxyList = new ArrayList<TemplateDirectiveProxy>();

    // --------- /Populated During Initialization --------- //

    // --------- LifeCycle --------- //
    public void init() {
        if (!initialized) {

            // initialize the hibernateSessionFactoryBuilder
            if (hibernateSessionFactoryBuilder != null && hibernateSessionFactoryBuilder instanceof Initializable) {
                ((Initializable) hibernateSessionFactoryBuilder).init();
            }

            // register the webParameterParsers
            if (webParameterParsers != null) {
                for (WebParameterParser webParameterParser : webParameterParsers) {
                    registerWebParameterParser(webParameterParser);
                }
            }

            // register the webhandlers
            
            if (webHandlers != null) {
                for (Object webHandler : webHandlers) {
                    registerWebHandlerMethods(webHandler);
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

        Map m = rc.getWebMap();
        
        processWebModels(m,rc);
        
        String path = rc.popFramePath();
        if (path == null){
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
            data = rc.getWebActionResponse();
        }
        // otherwise, we process the webmodels
        else {
            Map m = new HashMap();

            processWebModels(m,rc);

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
    WebActionResponse processWebAction(String actionName, RequestContext rc) throws Throwable {
        WebActionHandlerRef webActionRef = getWebActionRef(actionName);

        if (webActionRef == null) {
            throw new SnowRuntimeException(Error.NO_WEB_ACTION, "WebAction", actionName);
        }

        // --------- Invoke Method --------- //
        boolean invokeWebAction = true;

        Object result = null;

        try {
            if (webHandlerInterceptor != null) {
                invokeWebAction = webHandlerInterceptor.before(webActionRef.getMethod(), rc);
            }

            if (invokeWebAction) {
                result = webActionRef.invokeWebAction(rc);
            }

            if (webHandlerInterceptor != null) {
                webHandlerInterceptor.after(webActionRef.getMethod(), rc);
            }
        } catch (Throwable t) {
            processWebExceptionHandler(t, rc);
        }
        // --------- /Invoke Method --------- //

        WebActionResponse response = new WebActionResponse(result);
        return response;
    }

    void processWebModels(Map m, RequestContext rc) throws Throwable {
        // get the rootModelBuilder
        WebModelHandlerRef rootWmr = getWebModelRef("/");
        if (rootWmr != null) {
            invokeWebModelRef(rootWmr, m, rc);
        }
        
        
        StringBuilder pathBuilder = new StringBuilder();
        String[] resourcePaths = rc.getResourcePaths();
        for (int i = 0; i < resourcePaths.length; i++) {
            String path = pathBuilder.append('/').append(resourcePaths[i]).toString();
            WebModelHandlerRef webModelRef = getWebModelRef(path);
            invokeWebModelRef(webModelRef, m, rc);
        }        

        // Match and process the "matches" webModels
        List<WebModelHandlerRef> matchWebModelRefs = getMatchWebModelRef(pathBuilder.toString());
        for (WebModelHandlerRef webModelRef : matchWebModelRefs) {
            invokeWebModelRef(webModelRef, m, rc);
        }
    }

    private void invokeWebModelRef(WebModelHandlerRef webModelRef, Map m, RequestContext rc) throws Throwable {

        if (webModelRef != null) {

            boolean invokeWebModel = true;

            try {
                if (webHandlerInterceptor != null) {
                    invokeWebModel = webHandlerInterceptor.before(webModelRef.getMethod(), rc);
                }

                if (invokeWebModel) {
                    webModelRef.invokeWebModel(m, rc);
                }

                if (webHandlerInterceptor != null) {
                    webHandlerInterceptor.after(webModelRef.getMethod(), rc);
                }
            } catch (Throwable e) {
                processWebExceptionHandler(e, rc);
            }

        }
    }

    boolean processWebFile(RequestContext rc) throws Throwable{
        WebResourceHandlerRef webFileRef = getWebFileRef(rc.getResourcePath());
        if (webFileRef != null){
            webFileRef.invokeWebFile(rc);
            return true;
        }else{
            return false;
        }
    }
    
    void processWebExceptionHandler(Throwable e, RequestContext rc) throws Throwable {
        Throwable t = null;

        if (e instanceof InvocationTargetException) {
            t = ((InvocationTargetException) e).getCause();
        }

        if (t != null) {
            WebExceptionHandlerRef ref = getWebExceptionRef(t.getClass());

            // if we find the issue
            if (ref != null) {
                ref.invokeWebExceptionHandler(t, rc);
                // TODO: miwht want to try catch, and throw the cause as well
                // (to be consistent
            } else {
                throw t;
            }
        } else {
            throw e;
        }
    }

    // --------- /WebHandler Processing --------- //
    
    public String getTemplatePath(String resourcePath){
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
    
    // --------- Ref Getters --------- //
    WebActionHandlerRef getWebActionRef(String actionName) {
        return webActionDic.get(actionName);
    }

    WebModelHandlerRef getWebModelRef(String path) {
        return webModelByStartsWithMap.get(path);
    }

    List<WebModelHandlerRef> getMatchWebModelRef(String fullPriPath) {
        List<WebModelHandlerRef> matchWebModelRefs = new ArrayList<WebModelHandlerRef>();

        for (WebModelHandlerRef webModelRef : webModelRefList) {
            // System.out.println("WebModule.getMatchWebModeulRef: " +
            // webModelRef.toString());
            boolean match = webModelRef.matchesPath(fullPriPath);
            if (match) {
                matchWebModelRefs.add(webModelRef);
            }
        }

        return matchWebModelRefs;
    }

    WebResourceHandlerRef getWebFileRef(String path) {
        for (WebResourceHandlerRef webFileRef : webFileList) {
            boolean match = webFileRef.matchesPath(path);
            if (match) {
                return webFileRef;
            }
        }
        return null;
    }

    WebExceptionHandlerRef getWebExceptionRef(Class<? extends Throwable> exceptionClass) {
        WebExceptionHandlerRef ref = null;

        do {
            ref = webExceptionHanderMap.get(exceptionClass);
            if (ref != null) {
                return ref;
            }
            Class clzz = exceptionClass.getSuperclass();

            // TODO: we need to test this one further
            if (Throwable.class.isAssignableFrom(clzz)) {
                exceptionClass = (Class<? extends Throwable>) clzz;
            } else {
                return null;
            }

        } while (exceptionClass != null);

        return ref;
    }

    // --------- /Ref Getters --------- //

    // --------- Registration Methods (call at init() time) --------- //
    private void registerWebParameterParser(WebParameterParser webParameterParser) {
        Class<? extends Annotation> annotation = webParameterParser.getAnnotationClass();
        if (webParameterParserMap.containsKey(annotation)) {
            throw new IllegalStateException("multiple web parameter parsers configured for annotation class : " + annotation.getName());
        }

        webParameterParserMap.put(annotation, webParameterParser);
    }

    private final void registerWebHandlerMethods(Object targetObject) {
        Class c = targetObject.getClass();

        Method methods[] = c.getMethods();
        List<String> additionalLeafPaths = new ArrayList<String>();

        for (Method m : methods) {
            // Annotation[] as = m.getAnnotations();

            // --------- Register Web Action --------- //
            WebActionHandler action = m.getAnnotation(WebActionHandler.class);
            // if it is an action method, then, add the WebAction Object and
            // Method to the action Dic
            if (action != null) {
                registerWebAction(targetObject, m, action);
            }
            // --------- /Register Web Action --------- //

            // --------- Register Web Model --------- //
            WebModelHandler modelBuilder = m.getAnnotation(WebModelHandler.class);
            if (modelBuilder != null) {
                registerWebModel(targetObject, m, modelBuilder);

                // if this is for a leaf path, grab the startWith values from the
                // the web model handler annotation.
                // todo - warn if startsWith has no entries which has no effect?
                if (modelBuilder.leafPath()) {
                    String[] leafPaths = modelBuilder.startsWith();

                    // make sure they all have trailing slashes...
                    for (int i = 0; i < leafPaths.length; i++) {
                        if (!leafPaths[i].endsWith("/")) {
                            leafPaths[i] += "/";
                        }
                    }

                    additionalLeafPaths.addAll(Arrays.asList(leafPaths));
                }
            }
            // --------- Register Web Model --------- //

            // --------- Register Web File --------- //
            WebResourceHandler webFile = m.getAnnotation(WebResourceHandler.class);
            if (webFile != null) {
                registerWebFile(targetObject, m, webFile);
            }
            // --------- /Register Web File --------- //

            // --------- Register WebException --------- //
            WebExceptionHandler webExceptionHandler = m.getAnnotation(WebExceptionHandler.class);
            if (webExceptionHandler != null) {
                registerWebExceptionHandler(targetObject, m, webExceptionHandler);
            }
            // --------- /Register WebException --------- //

            // --------- Register Web Template Directive --------- //
            WebTemplateDirectiveHandler webTemplateDirective = m.getAnnotation(WebTemplateDirectiveHandler.class);
            if (webTemplateDirective != null) {
                registerWebTemplateDirective(targetObject, m, webTemplateDirective);
            }
            // --------- /Register Web Template Directive --------- //
        }
        
        // if we have any declared leaf paths, add them into the array.  they come after
        // any injected leaf path values.
        if(additionalLeafPaths.size() > 0) {
            if(leafPaths != null) {
                additionalLeafPaths.addAll(0, Arrays.asList(leafPaths));
            }

            leafPaths = additionalLeafPaths.toArray(new String[additionalLeafPaths.size()]);
        }        
    }

    private final void registerWebModel(Object webHandler, Method m, WebModelHandler webModel) {
        // System.out.println("Register WebModel " + getName() + " - " +
        // m.getName());

        WebModelHandlerRef webModelRef = new WebModelHandlerRef(webHandler, m, webParameterParserMap, webModel);
        webModelRefList.add(webModelRef);

        String startWithArray[] = webModel.startsWith();
        for (String startsWith : startWithArray) {
            webModelByStartsWithMap.put(startsWith, webModelRef);
        }
    }

    private final void registerWebAction(Object webHandler, Method m, WebActionHandler webAction) {

        String actionName = webAction.name();
        // if the action does have an empty name, then, take the name of the
        // method
        if (actionName.length() == 0) {
            actionName = m.getName();
        }
        // try to get the actionObjectList from the actionDic
        WebActionHandlerRef actionRef = webActionDic.get(actionName);
        // if the WebActionRef already exist, throw an exception
        if (actionRef != null) {
            // AlertHandler.systemSevere(Alert.ACTION_NAME_ALREADY_EXIST,
            // actionName);
            throw new RuntimeException("Action Name Already Exist: " + actionName);
        }
        // if not found, create an empty list
        // System.out.println("WebModule.registerWebAction: " + getName() + ":"
        // + actionName);
        // add this object and method to the list
        webActionDic.put(actionName, new WebActionHandlerRef(webHandler, m, webParameterParserMap, webAction));
    }

    private final void registerWebFile(Object webHandler, Method m, WebResourceHandler webFile) {
        WebResourceHandlerRef webFileRef = new WebResourceHandlerRef(webHandler, m, webParameterParserMap, webFile);
        webFileList.add(webFileRef);
    }

    private final void registerWebExceptionHandler(Object webHandler, Method m, WebExceptionHandler webExceptionHandler) {
        WebExceptionHandlerRef webExcpetionHandlerRef = new WebExceptionHandlerRef(webHandler, m, webParameterParserMap, webExceptionHandler);
        webExceptionHanderMap.put(webExcpetionHandlerRef.getThrowableClass(), webExcpetionHandlerRef);
        // webFileList.add(webFileRef);
    }

    private final void registerWebTemplateDirective(Object webHandler, Method m,
                            WebTemplateDirectiveHandler webTemplateDirective) {
        String templateMethodName = webTemplateDirective.name();
        // if the action does have an empty name, then, take the name of the
        // method
        if (templateMethodName.length() == 0) {
            templateMethodName = m.getName();
        }

        WebTemplateDirectiveHandlerRef directiveRef = new WebTemplateDirectiveHandlerRef(webHandler, m, webParameterParserMap);
        TemplateDirectiveProxy directiveProxy = new TemplateDirectiveProxy(templateMethodName, directiveRef);
        templateDirectiveProxyList.add(directiveProxy);
    }

    // --------- R/egistration Methods (call at init() time) --------- //
}

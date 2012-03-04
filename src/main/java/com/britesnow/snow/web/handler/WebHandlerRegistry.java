package com.britesnow.snow.web.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.britesnow.snow.web.handler.annotation.WebActionHandler;
import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.handler.annotation.WebResourceHandler;
import com.britesnow.snow.web.handler.annotation.WebTemplateDirectiveHandler;
import com.britesnow.snow.web.param.resolver.WebParamResolverRef;
import com.britesnow.snow.web.param.resolver.WebParamResolverRegistry;
import com.britesnow.snow.web.renderer.freemarker.TemplateDirectiveProxy;
import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class WebHandlerRegistry {
    
    private String[]                         leafPaths;
    
    private Map<String, WebModelHandlerRef>  webModelHandlerByStartsWithMap = new HashMap<String, WebModelHandlerRef>();
    private List<WebModelHandlerRef>         webModelHandlerRefList         = new ArrayList<WebModelHandlerRef>();
    private Map<String, WebActionHandlerRef> webActionHandlerDic            = new HashMap<String, WebActionHandlerRef>();
    private List<WebResourceHandlerRef>      webResourceHandlerList         = new ArrayList<WebResourceHandlerRef>();
    private List<TemplateDirectiveProxy>     templateDirectiveProxyList     = new ArrayList<TemplateDirectiveProxy>();
    
    @Inject
    private WebParamResolverRegistry webParamResolverRegistry;
    
    /**
     * Must be called before calling registerWebHandlers.<br />
     * Must be called before at application init time (not thread safe). <br />
     */
    public void init(){
        webParamResolverRegistry.init();
    }
    
    
    public void registerWebHandlers(Object webHandler){
        registerWebHandlerMethods(webHandler);
    }
    
    /**
     * Get the leafPaths (probably 
     * @return
     */
    public String[] getLeafPaths(){
        return leafPaths;
    }
    
    public WebActionHandlerRef getWebActionHandlerRef(String actionName) {
        return webActionHandlerDic.get(actionName);
    }

    public WebModelHandlerRef getWebModeHandlerlRef(String path) {
        return webModelHandlerByStartsWithMap.get(path);
    }

    public List<WebModelHandlerRef> getMatchWebModelHandlerRef(String fullPriPath) {
        List<WebModelHandlerRef> matchWebModelRefs = new ArrayList<WebModelHandlerRef>();

        for (WebModelHandlerRef webModelRef : webModelHandlerRefList) {
            // System.out.println("WebModule.getMatchWebModeulRef: " +
            // webModelRef.toString());
            boolean match = webModelRef.matchesPath(fullPriPath);
            if (match) {
                matchWebModelRefs.add(webModelRef);
            }
        }

        return matchWebModelRefs;
    }

    public WebResourceHandlerRef getWebResourceHandlerRef(String path) {
        for (WebResourceHandlerRef webFileRef : webResourceHandlerList) {
            boolean match = webFileRef.matchesPath(path);
            if (match) {
                return webFileRef;
            }
        }
        return null;
    }
    
    // --------- Private Registration Methods (call at init() time) --------- //
    private final void registerWebHandlerMethods(Object targetObject) {
        Class c = targetObject.getClass();

        Method methods[] = c.getMethods();
        List<String> additionalLeafPaths = new ArrayList<String>();

        for (Method m : methods) {
            // Annotation[] as = m.getAnnotations();

            // --------- Register WebActionHandler --------- //
            WebActionHandler webActionHandlerAnnotation = m.getAnnotation(WebActionHandler.class);
            // if it is an action method, then, add the WebAction Object and
            // Method to the action Dic
            if (webActionHandlerAnnotation != null) {
                registerWebAction(targetObject, m, webActionHandlerAnnotation);
            }
            // --------- /Register WebActionHandler --------- //

            // --------- Register WebModelHandler --------- //
            WebModelHandler webModelHandlerAnnotation = m.getAnnotation(WebModelHandler.class);
            if (webModelHandlerAnnotation != null) {
                registerWebModel(targetObject, m, webModelHandlerAnnotation);

                // if this is for a leaf path, grab the startWith values from the
                // the web model handler annotation.
                // todo - warn if startsWith has no entries which has no effect?
                if (webModelHandlerAnnotation.leafPath()) {
                    String[] leafPaths = webModelHandlerAnnotation.startsWith();
                    // make sure they all have trailing slashes...
                    for (int i = 0; i < leafPaths.length; i++) {
                        if (!leafPaths[i].endsWith("/")) {
                            leafPaths[i] += "/";
                        }
                    }

                    additionalLeafPaths.addAll(Arrays.asList(leafPaths));
                }
            }
            // --------- Register WebModelHandler --------- //

            // --------- Register WebResourceHandler --------- //
            WebResourceHandler webResourceHandlerAnnotation = m.getAnnotation(WebResourceHandler.class);
            if (webResourceHandlerAnnotation != null) {
                registerResourceHandler(targetObject, m, webResourceHandlerAnnotation);
            }
            // --------- /Register WebResourceHandler --------- //

            // --------- Register Web Template Directive --------- //
            WebTemplateDirectiveHandler webTemplateDirective = m.getAnnotation(WebTemplateDirectiveHandler.class);
            if (webTemplateDirective != null) {
                registerWebTemplateDirective(targetObject, m, webTemplateDirective);
            }
            // --------- /Register Web Template Directive --------- //
        }

        // if we have any declared leaf paths, add them into the array. they come after
        // any injected leaf path values.
        if (additionalLeafPaths.size() > 0) {
            if (leafPaths != null) {
                additionalLeafPaths.addAll(0, Arrays.asList(leafPaths));
            }

            leafPaths = additionalLeafPaths.toArray(new String[additionalLeafPaths.size()]);
        }
    }

    private final void registerWebModel(Object webHandler, Method m, WebModelHandler webModel) {
        // System.out.println("Register WebModel " + getName() + " - " +
        // m.getName());
        WebParamResolverRef webParamResolverRefs[] = buildWebParamResolverRefs(m);
        WebModelHandlerRef webModelRef = new WebModelHandlerRef(webHandler, m, webParamResolverRefs,webModel);
        webModelHandlerRefList.add(webModelRef);

        String startWithArray[] = webModel.startsWith();
        for (String startsWith : startWithArray) {
            webModelHandlerByStartsWithMap.put(startsWith, webModelRef);
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
        WebActionHandlerRef actionRef = webActionHandlerDic.get(actionName);
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
        WebParamResolverRef webParamResolverRefs[] = buildWebParamResolverRefs(m);
        webActionHandlerDic.put(actionName, new WebActionHandlerRef(webHandler, m,webParamResolverRefs, webAction));
    }

    private final void registerResourceHandler(Object webHandler, Method m, WebResourceHandler webResourceHandler) {
        WebParamResolverRef webParamResolverRefs[] = buildWebParamResolverRefs(m);
        WebResourceHandlerRef webFileRef = new WebResourceHandlerRef(webHandler, m, webParamResolverRefs, webResourceHandler);
        webResourceHandlerList.add(webFileRef);
    }

    private final void registerWebTemplateDirective(Object webHandler, Method m,
                            WebTemplateDirectiveHandler webTemplateDirective) {
        String templateMethodName = webTemplateDirective.name();
        // if the action does have an empty name, then, take the name of the
        // method
        if (templateMethodName.length() == 0) {
            templateMethodName = m.getName();
        }

        WebTemplateDirectiveHandlerRef directiveRef = new WebTemplateDirectiveHandlerRef(webHandler, m);
        TemplateDirectiveProxy directiveProxy = new TemplateDirectiveProxy(templateMethodName, directiveRef);
        templateDirectiveProxyList.add(directiveProxy);
    }

    // --------- /Private Registration Methods (call at init() time) --------- //
    
    private WebParamResolverRef[] buildWebParamResolverRefs(Method webHandlerMethod){
        Class[] paramTypes = webHandlerMethod.getParameterTypes();
        WebParamResolverRef[] webParamResolverRefs = new WebParamResolverRef[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++){
            webParamResolverRefs[i] = webParamResolverRegistry.getWebParamResolverRef(webHandlerMethod, i);
        }
        
        return webParamResolverRefs;
    }
    
    
}

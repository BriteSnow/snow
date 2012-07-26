package com.britesnow.snow.web.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.britesnow.snow.web.binding.WebObjects;
import com.britesnow.snow.web.exception.WebExceptionCatcherRef;
import com.britesnow.snow.web.exception.annotation.WebExceptionCatcher;
import com.britesnow.snow.web.handler.annotation.FreemarkerMethodHandler;
import com.britesnow.snow.web.handler.annotation.WebActionHandler;
import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.handler.annotation.WebResourceHandler;
import com.britesnow.snow.web.handler.annotation.FreemarkerDirectiveHandler;
import com.britesnow.snow.web.param.resolver.WebParamResolverRef;
import com.britesnow.snow.web.param.resolver.WebParamResolverRegistry;
import com.britesnow.snow.web.renderer.freemarker.FreemarkerDirectiveProxy;
import com.britesnow.snow.web.renderer.freemarker.FreemarkerMethodProxy;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class WebObjectRegistry {

    private String[]                                                leafPaths;

    private Map<String, WebModelHandlerRef>                         webModelHandlerByStartsWithMap = new HashMap<String, WebModelHandlerRef>();
    private List<WebModelHandlerRef>                                webModelHandlerRefList         = new ArrayList<WebModelHandlerRef>();
    private Map<String, WebActionHandlerRef>                        webActionHandlerDic            = new HashMap<String, WebActionHandlerRef>();
    private List<WebResourceHandlerRef>                             webResourceHandlerList         = new ArrayList<WebResourceHandlerRef>();
    private List<FreemarkerDirectiveProxy>                          freemarkerDirectiveProxyList   = new ArrayList<FreemarkerDirectiveProxy>();
    private List<FreemarkerMethodProxy>                             freemarkerMethodProxyList      = new ArrayList<FreemarkerMethodProxy>();
    private Map<Class<? extends Throwable>, WebExceptionCatcherRef> webExceptionCatcherMap         = new HashMap<Class<? extends Throwable>, WebExceptionCatcherRef>();

    @Inject
    private WebParamResolverRegistry                                webParamResolverRegistry;

    @Inject(optional = true)
    @Nullable
    @WebObjects
    private Object[]                                                webObjects;
    


    
    /**
     * Must be called before calling registerWebHandlers.<br />
     * Must be called before at application init time (not thread safe). <br />
     */
    public void init() {
        webParamResolverRegistry.init();

        if (webObjects != null) {
            for (Object webObject : webObjects) {
                registerWebObject(webObject);
            }
        }
    }

    private void registerWebObject(Object webHandler) {
        registerWebHandlerMethods(webHandler);
    }

    /**
     * Get the leafPaths (probably
     * 
     * @return
     */
    public String[] getLeafPaths() {
        return leafPaths;
    }

    public WebActionHandlerRef getWebActionHandlerRef(String actionName) {
        return webActionHandlerDic.get(actionName);
    }

    public WebModelHandlerRef getWebModeHandlerlRef(String path) {
        return webModelHandlerByStartsWithMap.get(path);
    }

    public List<WebModelHandlerRef> getMatchWebModelHandlerRef(String path) {
        List<WebModelHandlerRef> matchWebModelRefs = new ArrayList<WebModelHandlerRef>();

        for (WebModelHandlerRef webModelRef : webModelHandlerRefList) {
            boolean match = webModelRef.matchesPath(path);
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

    /**
     * Do not call. Internal to Snow.
     * 
     * (used by the FreemarkerTemplateRenderer)
     * 
     * @return
     */
    public List<FreemarkerDirectiveProxy> getFreemarkerDirectiveProxyList() {
        return freemarkerDirectiveProxyList;
    }
    
    /**
     * Do not call. Internal to Snow. 
     * 
     * @param exceptionClass
     * @return
     */
    public List<FreemarkerMethodProxy> getFreemarkerMethodProxyList(){
        return freemarkerMethodProxyList;
    }

    public WebExceptionCatcherRef getWebExceptionCatcherRef(Class<? extends Throwable> exceptionClass) {
        WebExceptionCatcherRef ref = null;

        // if there is a direct match, return it.
        ref = webExceptionCatcherMap.get(exceptionClass);
        if (ref != null) {
            return ref;
        }

        Class cls = exceptionClass.getSuperclass();

        while (cls != Object.class) {
            ref = webExceptionCatcherMap.get(cls);
            if (ref != null) {
                return ref;
            }
            cls = cls.getSuperclass();
        }

        return null;
    }

    // --------- Private Registration Methods (call at init() time) --------- //
    private final void registerWebHandlerMethods(Object targetObject) {

        Class c = getNonGuiceEnhancedClass(targetObject);

        Method methods[] = c.getMethods();
        List<String> additionalLeafPaths = new ArrayList<String>();

        for (Method m : methods) {
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
                if (webModelHandlerAnnotation.leaf()) {
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
                registerWebResourceHandler(targetObject, m, webResourceHandlerAnnotation);
            }
            // --------- /Register WebResourceHandler --------- //

            // --------- Freemarker handlers --------- //
            FreemarkerDirectiveHandler webTemplateDirective = m.getAnnotation(FreemarkerDirectiveHandler.class);
            if (webTemplateDirective != null) {
                registerFreemarkerDirective(targetObject, m, webTemplateDirective);
            }
            
            FreemarkerMethodHandler freemarkerMethodHandler = m.getAnnotation(FreemarkerMethodHandler.class);
            if (freemarkerMethodHandler != null){
                registerFreemarkerMethod(targetObject, m, freemarkerMethodHandler);
            }
            // --------- /Freemarker handlers --------- //

            // --------- Register WebException --------- //
            WebExceptionCatcher webExceptionHandler = m.getAnnotation(WebExceptionCatcher.class);
            if (webExceptionHandler != null) {
                registerWebExceptionCatcher(targetObject, m, webExceptionHandler);
            }
            // --------- /Register WebException --------- //

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
        WebParamResolverRef webParamResolverRefs[] = buildWebParamResolverRefs(m);
        WebModelHandlerRef webModelRef = new WebModelHandlerRef(webHandler, m, webParamResolverRefs, webModel);
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
        // add this object and method to the list
        WebParamResolverRef webParamResolverRefs[] = buildWebParamResolverRefs(m);
        webActionHandlerDic.put(actionName, new WebActionHandlerRef(webHandler, m, webParamResolverRefs, webAction));
    }

    private final void registerWebResourceHandler(Object webHandler, Method m, WebResourceHandler webResourceHandler) {
        WebParamResolverRef webParamResolverRefs[] = buildWebParamResolverRefs(m);
        WebResourceHandlerRef webFileRef = new WebResourceHandlerRef(webHandler, m, webParamResolverRefs, webResourceHandler);
        webResourceHandlerList.add(webFileRef);
    }

    private final void registerFreemarkerDirective(Object webHandler, Method m,
                            FreemarkerDirectiveHandler freemarkerDirectiveHandler) {
        String templateMethodName = freemarkerDirectiveHandler.name();
        // if the action does have an empty name, then, take the name of the
        // method
        if (templateMethodName.length() == 0) {
            templateMethodName = m.getName();
        }

        WebParamResolverRef webParamResolverRefs[] = buildWebParamResolverRefs(m);
        FreemakerDirectiveHandlerRef directiveRef = new FreemakerDirectiveHandlerRef(webHandler, m, webParamResolverRefs, freemarkerDirectiveHandler);
        FreemarkerDirectiveProxy directiveProxy = new FreemarkerDirectiveProxy(templateMethodName, directiveRef);
        freemarkerDirectiveProxyList.add(directiveProxy);
    }
    
    private final void registerFreemarkerMethod(Object webHandler, Method m, FreemarkerMethodHandler freemarkerMethodHandler){
        String name = freemarkerMethodHandler.name();
        if (name.length() == 0){
            name = m.getName();
        }
        WebParamResolverRef webParamResolverRefs[] = buildWebParamResolverRefs(m);
        FreemarkerMethodHandlerRef ref = new FreemarkerMethodHandlerRef(webHandler,m, webParamResolverRefs,freemarkerMethodHandler);
        FreemarkerMethodProxy proxy = new FreemarkerMethodProxy(name,ref);
        freemarkerMethodProxyList.add(proxy);
        
    }

    private final void registerWebExceptionCatcher(Object webHandler, Method m, WebExceptionCatcher webExceptionHandler) {
        WebExceptionCatcherRef webExcpetionCatcherRef = new WebExceptionCatcherRef(webHandler, m, webExceptionHandler);
        webExceptionCatcherMap.put(webExcpetionCatcherRef.getThrowableClass(), webExcpetionCatcherRef);
    }

    // --------- /Private Registration Methods (call at init() time) --------- //

    private WebParamResolverRef[] buildWebParamResolverRefs(Method webHandlerMethod) {
        Class[] paramTypes = webHandlerMethod.getParameterTypes();
        WebParamResolverRef[] webParamResolverRefs = new WebParamResolverRef[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            webParamResolverRefs[i] = webParamResolverRegistry.getWebParamResolverRef(webHandlerMethod, i);
        }

        return webParamResolverRefs;
    }

    public static Class getNonGuiceEnhancedClass(Object obj) {
        String className = obj.getClass().getName();
        if (className.indexOf("$$EnhancerByGuice$$") > -1) {
            return obj.getClass().getSuperclass();
        } else {
            return obj.getClass();
        }
    }

}

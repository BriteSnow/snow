package com.britesnow.snow.web.param.resolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.britesnow.snow.web.binding.WebObjects;
import com.britesnow.snow.web.handler.WebObjectRegistry;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.britesnow.snow.web.param.resolver.annotation.WebParamResolver;
import com.britesnow.snow.web.renderer.freemarker.FreemarkerParamResolvers;
import com.google.inject.Inject;

@Singleton
public class WebParamResolverRegistry {

    static private final Logger logger = LoggerFactory.getLogger(WebParamResolverRegistry.class);
    
    private Map<Class, WebParamResolverRef> refByReturnType = new HashMap<Class, WebParamResolverRef>();
    // for now, just support one annotation (ignore the WebParam)
    private Map<Class, WebParamResolverRef> refByAnnotation = new HashMap<Class, WebParamResolverRef>();

    @Inject
    private SystemWebParamResolvers systemWebParamResolvers;
    
    @Inject
    private FreemarkerParamResolvers freemarkerParamResolvers;
    
    
    @Inject(optional = true)
    @Nullable
    @WebObjects
    private Object[]                       webObjects;       
    
    /**
     * Must be called before calling registerResolvers. 
     * Must be called at init time, no thread safe
     */
    public void init(){
        // first register the SystemWebParamResolvers
        registerWebParamResolvers(systemWebParamResolvers);
        
        registerWebParamResolvers(freemarkerParamResolvers);
        
        // then, register the applicaiton WebParamResolvers
        if (webObjects != null){
            for (Object webObject : webObjects){
                registerWebParamResolvers(webObject);
            }
        }
        
    }
    
    
    final private void registerWebParamResolvers(Object resolversObject) {

        Class cls = WebObjectRegistry.getNonGuiceEnhancedClass(resolversObject);

        for (Method method : cls.getMethods()) {
            WebParamResolver webParamResolver = method.getAnnotation(WebParamResolver.class);

            if (webParamResolver != null) {
                WebParamResolverRef ref = new WebParamResolverRef(webParamResolver, resolversObject, method);
                Class returnType = ref.getReturnType();
                Class[] annotatedWith = ref.getAnnotatedWith();

                if (annotatedWith.length == 0) {
                    refByReturnType.put(returnType, ref);
                }else{
                    // for now, support only one annotation
                    // TODO: need to add support for multi annotation
                    refByAnnotation.put(annotatedWith[0],ref);
                }
            }

        }

    }
    
    /**
     * This will return the WebParamResolverRef for webHandlerMethod param at the index paramIdx
     * 
     * @param webHandlerMethod
     * @param paramIdx
     * @return
     */
    public WebParamResolverRef getWebParamResolverRef(Method webHandlerMethod, int paramIdx) {
        WebParamResolverRef ref = null;

        
        Class paramType = webHandlerMethod.getParameterTypes()[paramIdx];

        Annotation[] paramAnnotations = webHandlerMethod.getParameterAnnotations()[paramIdx];
        Annotation paramAnnotation = getFirstAnnotationButWebParam(paramAnnotations);
        
        // if we have an annotation, it takes precedence
        
        // first try to get the annotation 
        // TODO: need to support multiple annotations
        if (paramAnnotation != null){
            ref = refByAnnotation.get(paramAnnotation.annotationType());
        }

        // TODO: probably need to mature this logic to make sure the system is the most predictable. 
        
        // if not found, then, got with the paramType
        if (ref == null){
            ref = refByReturnType.get(paramType);
        }
        
        // if still null, then, check the parent classes
        if (ref == null){
            Class parentClass = paramType.getSuperclass();
            while (parentClass != null && ref == null && parentClass != Object.class){
                ref = refByReturnType.get(parentClass);
                parentClass = parentClass.getSuperclass();
            }
        }
        
        // if still null, then, check with the interfaces
        if (ref == null){
            for (Class interfaceClass : paramType.getInterfaces()){
                ref = refByReturnType.get(interfaceClass);
                if (ref != null){
                    break;
                }
            }
        }
        
        if (ref == null){
            logger.error("Snow Fatal Error: No Param Resolver found for param type '" + paramType.getCanonicalName() + 
                "' or annotation '" + paramAnnotation.toString() + "' in the method '" + webHandlerMethod.getName() + "' of the class '" + webHandlerMethod.getDeclaringClass().getName() +
                "'. Make sure to have the appropriate @ParamResolver for the param type or associated annotations");
        }
        
        return ref;
    }
    
    
    private static Annotation getFirstAnnotationButWebParam(Annotation[] paramAnnotations){
        for (Annotation a : paramAnnotations){
            if (a.annotationType() != WebParam.class){
                return a;
            }
        }
        return null;
    }
    
    
}

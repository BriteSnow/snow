package com.britesnow.snow.web.param.resolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.britesnow.snow.web.binding.WebClasses;
import com.britesnow.snow.web.param.resolver.annotation.WebParamResolver;
import com.britesnow.snow.web.renderer.freemarker.FreemarkerParamResolvers;
import com.google.inject.Inject;

@Singleton
public class WebParamResolverRegistry {

    @SuppressWarnings("unused")
    static private final Logger                                               logger          = LoggerFactory.getLogger(WebParamResolverRegistry.class);

    private Map<Class, WebParamResolverRef>                                   refByReturnType = new HashMap<Class, WebParamResolverRef>();

    // for now, just support one annotation
    private Map<Class<? extends Annotation>, Map<Class, WebParamResolverRef>> refByAnnotation = new HashMap<Class<? extends Annotation>, Map<Class, WebParamResolverRef>>();

    @Inject(optional = true)
    @Nullable
    @WebClasses
    private Class[]                                                          webClasses;

    /**
     * Must be called before calling registerResolvers. Must be called at init time, no thread safe
     */
    public void init() {
        // first register the SystemWebParamResolvers
        registerWebParamResolvers(SystemWebParamResolvers.class);
        registerWebParamResolvers(FreemarkerParamResolvers.class);

        // then, register the applicaiton WebParamResolvers
        if (webClasses != null) {
            for (Class webClass : webClasses) {
                registerWebParamResolvers(webClass);
            }
        }

    }

    final private void registerWebParamResolvers(Class webResolverClass) {

        //Class cls = WebObjectRegistry.getNonGuiceEnhancedClass(resolversObject);
        Class cls = webResolverClass;

        for (Method method : cls.getMethods()) {
            WebParamResolver webParamResolver = method.getAnnotation(WebParamResolver.class);

            if (webParamResolver != null) {
                WebParamResolverRef ref = new WebParamResolverRef(webParamResolver, cls, method);
                Class returnType = ref.getReturnType();
                Class[] annotatedWith = ref.getAnnotatedWith();

                if (annotatedWith.length == 0) {
                    refByReturnType.put(returnType, ref);
                } else {
                    // for now, support only one annotation
                    // TODO: need to add support for multi annotation
                    Class<? extends Annotation> anCls = annotatedWith[0];
                    Map<Class, WebParamResolverRef> refByType = refByAnnotation.get(anCls);
                    if (refByType == null) {
                        refByType = new HashMap<Class, WebParamResolverRef>();
                        refByAnnotation.put(anCls, refByType);
                    }
                    refByType.put(returnType, ref);
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
        Annotation paramAnnotation = getFirstAnnotation(paramAnnotations);

        // if we have an annotation, it takes precedence

        // first try to get the annotation
        // TODO: need to support multiple annotations
        if (paramAnnotation != null) {
            Map<Class, WebParamResolverRef> refByType = refByAnnotation.get(paramAnnotation.annotationType());
            if (refByType != null) {
                for (Class type : refByType.keySet()) {
                    //if (paramType.isAssignableFrom(type)){
                    
                    if (type.isAssignableFrom(paramType)){
                        ref = findRefForType(paramType,refByType);
                        if (ref != null) {
                            break;
                        }
                    }
                }
            }
        }

        // if could not resolve it with the annotation, try with the type only.
        if (ref == null) {
            ref = findRefForType(paramType,refByReturnType);
        }

        return ref;
    }

    private WebParamResolverRef findRefForType(Class paramType,Map<Class,WebParamResolverRef> refByType) {
        WebParamResolverRef ref = null;
        ref = refByType.get(paramType);
        // if still null, then, check the parent classes
        if (ref == null) {
            Class parentClass = paramType.getSuperclass();
            while (parentClass != null && ref == null) {
                ref = refByType.get(parentClass);
                parentClass = parentClass.getSuperclass();
            }
        }
        return ref;
    }

    private static Annotation getFirstAnnotation(Annotation[] paramAnnotations) {
        if (paramAnnotations.length > 0){
            return paramAnnotations[0];
        }else{
            return null;
        }
    }

}

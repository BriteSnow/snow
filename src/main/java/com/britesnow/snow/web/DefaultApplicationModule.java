package com.britesnow.snow.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.Nullable;


import com.britesnow.snow.web.binding.ApplicationPackageBase;
import com.britesnow.snow.web.binding.WebHandlerClasses;
import com.britesnow.snow.web.binding.WebHandlers;
import com.britesnow.snow.web.handler.annotation.WebActionHandler;
import com.britesnow.snow.web.handler.annotation.WebExceptionHandler;
import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.handler.annotation.WebResourceHandler;
import com.britesnow.snow.web.handler.annotation.WebTemplateDirectiveHandler;
import com.britesnow.snow.web.path.DefaultFramePathResolver;
import com.britesnow.snow.web.path.DefaultResourcePathResolver;
import com.britesnow.snow.web.path.FramePathResolver;
import com.britesnow.snow.web.path.ResourcePathResolver;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;

import com.metapossum.utils.scanner.reflect.ClassesInPackageScanner;
import com.metapossum.utils.scanner.reflect.ExtendsClassResourceFilter;

public class DefaultApplicationModule extends AbstractModule {

    private String applicationPackageBase;

    public DefaultApplicationModule(String applicationPachageBase) {
        this.applicationPackageBase = applicationPachageBase;
    }

    @Override
    protected void configure() {
        bind(FramePathResolver.class).to(DefaultFramePathResolver.class);
        bind(ResourcePathResolver.class).to(DefaultResourcePathResolver.class);
        bind(ActionNameResolver.class).to(DefaultActionNameResolver.class);

        if (applicationPackageBase != null) {
            bind(String.class).annotatedWith(ApplicationPackageBase.class).toInstance(applicationPackageBase);
        }
    }

    @Provides
    @WebHandlers
    @Inject(optional = true)
    @Nullable
    public Object[] providesWebHandlers(Injector injector, @WebHandlerClasses Class[] webHandlerClasses) {

        if (webHandlerClasses != null) {
            Object[] webHandlers = new Object[webHandlerClasses.length];
            int i = 0;
            for (Class cls : webHandlerClasses) {

                Object webHandler = injector.getInstance(cls);
                webHandlers[i] = webHandler;
                i++;
            }
            return webHandlers;
        }

        return null;
    }

    @Provides
    @WebHandlerClasses
    public Class[] providesWebHandlerClasses() {
        if (applicationPackageBase != null) {
            ClassesInPackageScanner classScanner = new ClassesInPackageScanner();

            classScanner.setResourceFilter(new ExtendsClassResourceFilter(Object.class, true) {
                @Override
                public boolean acceptScannedResource(Class cls) {
                    for (Method method : cls.getDeclaredMethods()) {

                        if (method.getAnnotation(WebActionHandler.class) != null || method.getAnnotation(WebResourceHandler.class) != null
                                                || method.getAnnotation(WebModelHandler.class) != null
                                                || method.getAnnotation(WebTemplateDirectiveHandler.class) != null
                                                || method.getAnnotation(WebExceptionHandler.class) != null) {
                            return true;
                        }
                    }
                    return false;
                }
            });

            try {
                Set classSet = classScanner.findSubclasses(applicationPackageBase, Object.class);
                Class[] webHandlerClasses = new Class[classSet.size()];
                classSet.toArray(webHandlerClasses);
                return webHandlerClasses;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new RuntimeException("Failed to scan packages: " + applicationPackageBase);
            }
        }else{
            return new Class[0];
        }

    }
    /*
     * 
     * private Class[] scanForWebHandlerClasses(WebModuleConfig webModuleConfig) { Class[] webHandlerClasses =
     * scanForClasses(webModuleConfig.getClass(), new ClassesInPackageScanner.AcceptanceTest() {
     * 
     * @Override public boolean acceptClass(Class<?> cls) { for (Method method : cls.getDeclaredMethods()) { if
     * (method.getAnnotation(WebActionHandler.class) != null || method.getAnnotation(WebFileHandler.class) != null ||
     * method.getAnnotation(WebModelHandler.class) != null || method.getAnnotation(WebTemplateDirectiveHandler.class) !=
     * null || method.getAnnotation(WebExceptionHandler.class) != null) { return true; } } return false; } });
     * 
     * return webHandlerClasses; }
     * 
     * private Class[] scanForClasses(Class baseClass, org.snowfk.util.ClassesInPackageScanner.AcceptanceTest
     * acceptanceTest) { Set<Class<?>> classes;
     * 
     * try { // Note: the mobules have the same classloader as the application, // so, the getClass().getClassLoader()
     * if fine. classes = new ClassesInPackageScanner(baseClass.getPackage().getName(), getClass().getClassLoader(),
     * false, acceptanceTest).scan(true); } catch (IOException e) { throw new
     * IllegalStateException("unable to scan package for classes", e); }
     * 
     * return classes.toArray(new Class[classes.size()]);
     * 
     * }
     */
    /*--------- /Privates ---------*/
}

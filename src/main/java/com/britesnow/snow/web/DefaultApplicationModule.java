package com.britesnow.snow.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.Nullable;

import com.britesnow.snow.web.binding.ApplicationPackageBase;
import com.britesnow.snow.web.binding.WebClasses;
import com.britesnow.snow.web.binding.WebObjects;
import com.britesnow.snow.web.exception.annotation.WebExceptionCatcher;
import com.britesnow.snow.web.handler.annotation.WebActionHandler;
import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.handler.annotation.WebResourceHandler;
import com.britesnow.snow.web.handler.annotation.WebTemplateDirectiveHandler;
import com.britesnow.snow.web.param.resolver.annotation.WebParamResolver;
import com.britesnow.snow.web.path.DefaultFramePathsResolver;
import com.britesnow.snow.web.path.DefaultResourcePathResolver;
import com.britesnow.snow.web.path.FramePathsResolver;
import com.britesnow.snow.web.path.ResourcePathResolver;
import com.britesnow.snow.web.renderer.JsonLibJsonRenderer;
import com.britesnow.snow.web.renderer.JsonRenderer;
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
        bind(FramePathsResolver.class).to(DefaultFramePathsResolver.class);
        bind(ResourcePathResolver.class).to(DefaultResourcePathResolver.class);
        bind(ActionNameResolver.class).to(DefaultActionNameResolver.class);
        bind(JsonRenderer.class).to(JsonLibJsonRenderer.class);

        if (applicationPackageBase != null) {
            bind(String.class).annotatedWith(ApplicationPackageBase.class).toInstance(applicationPackageBase);
        }
    }

    @Provides
    @WebObjects
    @Inject
    @Nullable
    public Object[] providesWebObjects(Injector injector, @WebClasses Class[] webHandlerClasses) {

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
    @WebClasses
    public Class[] providesWebClasses() {
        if (applicationPackageBase != null) {
            ClassesInPackageScanner classScanner = new ClassesInPackageScanner();

            classScanner.setResourceFilter(new ExtendsClassResourceFilter(Object.class, true) {
                @Override
                public boolean acceptScannedResource(Class cls) {
                    for (Method method : cls.getDeclaredMethods()) {

                        if (method.getAnnotation(WebActionHandler.class) != null || method.getAnnotation(WebResourceHandler.class) != null
                                                || method.getAnnotation(WebModelHandler.class) != null
                                                || method.getAnnotation(WebTemplateDirectiveHandler.class) != null
                                                || method.getAnnotation(WebParamResolver.class) != null
                                                || method.getAnnotation(WebExceptionCatcher.class) != null) {
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
        } else {
            return new Class[0];
        }

    }

}

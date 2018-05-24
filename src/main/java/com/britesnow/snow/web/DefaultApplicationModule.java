package com.britesnow.snow.web;

import java.lang.reflect.Method;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import com.britesnow.snow.util.PackageScanner;
import com.britesnow.snow.web.binding.ApplicationPackageBase;
import com.britesnow.snow.web.binding.WebClasses;
import com.britesnow.snow.web.exception.annotation.WebExceptionCatcher;
import com.britesnow.snow.web.handler.annotation.FreemarkerMethodHandler;
import com.britesnow.snow.web.handler.annotation.WebActionHandler;
import com.britesnow.snow.web.handler.annotation.WebModelHandler;
import com.britesnow.snow.web.handler.annotation.WebResourceHandler;
import com.britesnow.snow.web.handler.annotation.FreemarkerDirectiveHandler;
import com.britesnow.snow.web.hook.annotation.WebApplicationHook;
import com.britesnow.snow.web.hook.annotation.WebRequestHook;
import com.britesnow.snow.web.param.resolver.annotation.WebParamResolver;
import com.britesnow.snow.web.path.DefaultFramePathsResolver;
import com.britesnow.snow.web.path.DefaultResourceFileLocator;
import com.britesnow.snow.web.path.DefaultResourcePathResolver;
import com.britesnow.snow.web.path.FramePathsResolver;
import com.britesnow.snow.web.path.ResourceFileLocator;
import com.britesnow.snow.web.path.ResourcePathResolver;
import com.britesnow.snow.web.renderer.JsonLibJsonRenderer;
import com.britesnow.snow.web.renderer.JsonRenderer;
import com.britesnow.snow.web.rest.ContentTypeResolver;
import com.britesnow.snow.web.rest.DefaultContentTypeResolver;
import com.britesnow.snow.web.rest.annotation.*;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;



import com.google.common.base.Predicate;

public class DefaultApplicationModule extends AbstractModule {

    private String applicationPackageBase;

    public DefaultApplicationModule(String applicationPachageBase) {
        this.applicationPackageBase = applicationPachageBase;
    }

    @Override
    protected void configure() {
        bind(FramePathsResolver.class).to(DefaultFramePathsResolver.class);
        
        bind(ResourcePathResolver.class).to(DefaultResourcePathResolver.class);
        bind(ResourceFileLocator.class).to(DefaultResourceFileLocator.class);
        
        bind(ContentTypeResolver.class).to(DefaultContentTypeResolver.class);
        
        bind(JsonRenderer.class).to(JsonLibJsonRenderer.class);

        if (applicationPackageBase != null) {
            bind(String.class).annotatedWith(ApplicationPackageBase.class).toInstance(applicationPackageBase);
        }
    }


    @Singleton
    @Provides
    @WebClasses
    public Class[] providesWebClasses() {
        if (applicationPackageBase != null) {
            try {
                Class[] webClasses = new PackageScanner(applicationPackageBase).findClasses(webClassPredicate);
                return webClasses;
            } catch (Throwable e1) {
                throw new RuntimeException("Cannot load automatically the @WebClasses in package " + applicationPackageBase, e1);
            }
        } else {
            return new Class[0];
        }

    }
    

    private static Predicate<Class> webClassPredicate = new Predicate<Class>() {


    	// NOTE: The Guava default Predicate.test does not seems to be taken in account or something.
		//       If we do not override it here, we get an compile error.
        @Override
        public boolean test(Class input) {
            return this.apply(input);
        }


        @Override
        public boolean apply(@Nullable Class cls) {
            for (Method method : cls.getDeclaredMethods()) {

                if (method.getAnnotation(WebActionHandler.class) != null || method.getAnnotation(WebResourceHandler.class) != null
                        || method.getAnnotation(WebModelHandler.class) != null
                        || method.getAnnotation(WebParamResolver.class) != null
                        || method.getAnnotation(WebExceptionCatcher.class) != null
                        || method.getAnnotation(WebApplicationHook.class) != null
                        || method.getAnnotation(WebRequestHook.class) != null
                        || method.getAnnotation(FreemarkerDirectiveHandler.class) != null
                        || method.getAnnotation(FreemarkerMethodHandler.class) != null
                        || method.getAnnotation(WebGet.class) != null
                        || method.getAnnotation(WebPost.class) != null
                        || method.getAnnotation(WebPut.class) != null
                        || method.getAnnotation(WebPatch.class) != null
                        || method.getAnnotation(WebDelete.class) != null
                        || method.getAnnotation(WebOptions.class) != null
                        || method.getAnnotation(WebSerializer.class) != null){
                    return true;
                }
            }
            return false;
        }
    };

}



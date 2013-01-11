package com.britesnow.snow.test.singletest.classes;

import javax.inject.Named;
import javax.inject.Singleton;

import com.britesnow.snow.web.hook.AppStep;
import com.britesnow.snow.web.hook.annotation.WebApplicationHook;

@Singleton
public class SimpleWebAppHooks {
    
    static public AppState[] appStates = new AppState[2];
    
    @WebApplicationHook(step=AppStep.INIT)
    public void appHookInit(AppState appState, @Named("testVal")String testVal){
        System.out.println("testVal: -" + testVal + "-");
        
        appState.init = true;
        appStates[0] = appState;
    }

    @WebApplicationHook(step=AppStep.SHUTDOWN)
    public void appHookShutdown(AppState appState, @Named("testVal")String testVal){
        System.out.println("testVal: -" + testVal + "-");
        appState.shutdown = true;
        appStates[1] = appState;
    }
    
    
}

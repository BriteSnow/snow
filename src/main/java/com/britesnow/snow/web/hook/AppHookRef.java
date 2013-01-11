package com.britesnow.snow.web.hook;

import java.lang.reflect.Method;

import com.britesnow.snow.web.handler.ParamDef;

public class AppHookRef extends HookRef {
    private AppStep step;
    
    public AppHookRef(Class cls, Method method, ParamDef[] paramDefs, AppStep step){
        super(cls,method,paramDefs);
        this.step = step;
    }
  
    public AppStep getStep() {
        return step;
    }
}

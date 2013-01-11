package com.britesnow.snow.web.hook;

import java.lang.reflect.Method;

import com.britesnow.snow.web.handler.ParamDef;

public class AppHookRef extends HookRef {
    private AppPhase step;
    
    public AppHookRef(Class cls, Method method, ParamDef[] paramDefs, AppPhase step, On on){
        super(cls,method,paramDefs,on);
        this.step = step;
    }
  
    public AppPhase getStep() {
        return step;
    }
}

package com.britesnow.snow.web.hook;

import java.lang.reflect.Method;

import com.britesnow.snow.web.handler.ParamDef;

public class ReqHookRef extends HookRef {
    private ReqStep step;
    
    ReqHookRef(Class cls, Method method, ParamDef[] paramDefs,ReqStep step) {
        super(cls,method,paramDefs);
        this.step = step;
    }

    public ReqStep getStep() {
        return step;
    }
}


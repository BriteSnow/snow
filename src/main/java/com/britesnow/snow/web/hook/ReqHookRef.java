package com.britesnow.snow.web.hook;

import java.lang.reflect.Method;

import com.britesnow.snow.web.handler.ParamDef;

public class ReqHookRef extends HookRef {
    private ReqPhase step;
    
    ReqHookRef(Class cls, Method method, ParamDef[] paramDefs,ReqPhase step, On on) {
        super(cls,method,paramDefs,on);
        this.step = step;
    }

    public ReqPhase getStep() {
        return step;
    }
}


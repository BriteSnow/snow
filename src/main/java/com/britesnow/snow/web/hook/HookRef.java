package com.britesnow.snow.web.hook;

import java.lang.reflect.Method;

import com.britesnow.snow.web.handler.ParamDef;

public class HookRef {

    private Class      cls;
    private Method     method;
    private ParamDef[] paramDefs;
    private On         on;

    HookRef(Class cls, Method method, ParamDef[] paramDefs, On on) {
        this.cls = cls;
        this.method = method;
        this.paramDefs = paramDefs;
        this.on = on;
    }

    public Class getCls() {
        return cls;
    }

    public Method getMethod() {
        return method;
    }

    public ParamDef[] getParamDefs() {
        return paramDefs;
    }
    
    public On getOn(){
        return on;
    }
}

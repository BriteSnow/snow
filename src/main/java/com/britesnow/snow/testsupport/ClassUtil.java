package com.britesnow.snow.testsupport;

import java.lang.reflect.Method;

public class ClassUtil {

    
    /**
     * Convenient getter that get the first method with this given name (disregard arguments). 
     * Should be use in testing only code. 
     * 
     * @param cls
     * @param methodName
     * @return
     */
    public static Method getFirstMethod(Class cls, String methodName){
       for (Method m : cls.getMethods()){
           if (m.getName().equals(methodName)){
               return m;
           }
       }
       return null;
    }
}

package com.britesnow.snow.test.apptest;

import org.junit.Test;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;


/**
 * Just for Development right now. 
 */
public class JsTest {


    @Test
    public void simpeJsTest(){
        Context cx = Context.enter();
        Scriptable scope = cx.initStandardObjects();
        Object wrappedOut = Context.javaToJS("toto" + "haha", scope);
        ScriptableObject.putProperty(scope, "t", wrappedOut);
        Object jsResult = cx.evaluateString(scope,"t + 'and' + 'titi'","<cmd>",1,null);
        System.out.println("jsResult: \n" + Context.toString(jsResult));
        Context.exit();

    }
}

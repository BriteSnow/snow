package com.britesnow.snow.test.apptest;

import org.junit.Assert;
import org.junit.Test;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


/**
 *
 */
public class JsTest {

    @Test
    public void simpeJsTest(){
        Context cx = Context.enter();
        Scriptable scope = cx.initStandardObjects();
        Object wrappedOut = Context.javaToJS("foo" + "bar", scope);
        ScriptableObject.putProperty(scope, "t", wrappedOut);
        Object jsResult = cx.evaluateString(scope,"t + 'and' + 'bar'","<cmd>",1,null);
        Assert.assertEquals("foobarandbar", jsResult);
        Context.exit();

    }
}

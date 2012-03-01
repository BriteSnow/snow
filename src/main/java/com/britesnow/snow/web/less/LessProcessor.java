package com.britesnow.snow.web.less;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;


/**
 * 
 * For now,code from https://github.com/asual/lesscss-engine/
 * 
 * TODO: needs to make a JsEngine to support Handlebars as well
 *
 */
public class LessProcessor {
    static private Logger logger = LoggerFactory.getLogger(LessProcessor.class);
    
    private Scriptable scope;
    private ClassLoader classLoader;
    @SuppressWarnings("unused")
    private Function compileString;
    private Function compileFile;

    private static final String JS_ROOT = "com/britesnow/snow/web/less/";
    //private static final String JS_ROOT = "META-INF/";
    private static final String CHARSET = "UTF-8";
    
    
    public LessProcessor(){
        try {
            logger.debug("Initializing LESS Engine.");
            classLoader = getClass().getClassLoader();
            //URL less = classLoader.getResource(JS_ROOT + "less.js");
            URL less = classLoader.getResource(JS_ROOT + "less-rhino-1.2.2.js");
            URL env = classLoader.getResource(JS_ROOT + "env.js");
            URL engine = classLoader.getResource(JS_ROOT + "engine.js");
            Context cx = Context.enter();
            logger.debug("Using implementation version: " + cx.getImplementationVersion());
            cx.setOptimizationLevel(9);
            Global global = new Global();
            global.init(cx);
            scope = cx.initStandardObjects(global);
            cx.evaluateReader(scope, new InputStreamReader(env.openConnection().getInputStream()), env.getFile(), 1, null);
            cx.evaluateString(scope, "lessenv.charset = '" + CHARSET + "';", "charset", 1, null);
            cx.evaluateString(scope, "lessenv.css = " + "true" + ";", "css", 1, null);
            cx.evaluateReader(scope, new InputStreamReader(less.openConnection().getInputStream()), less.getFile(), 1, null);
            cx.evaluateReader(scope, new InputStreamReader(engine.openConnection().getInputStream()), engine.getFile(), 1, null);
            compileString = (Function) scope.get("compileString", scope);
            compileFile = (Function) scope.get("compileFile", scope);
            Context.exit();
        } catch (Exception e) {
            logger.error("LESS Engine intialization failed.", e);
        }        
    }
    
    public String compile(File input) {
        String result = null;
        try {
            long time = System.currentTimeMillis();
            logger.debug("Compiling File: " + "file:" + input.getAbsolutePath());
            result = call(compileFile, new Object[] {"file:" + input.getAbsolutePath(), classLoader});
            logger.debug("The compilation of '" + input + "' took " + (System.currentTimeMillis () - time) + " ms.");
            
        } catch (Exception e) {
            try {
                parseLessException(e);
            }catch(Exception e2){
                Throwables.propagate(e2);
            }
            
        }
        
        return result;
    }    
    
    private synchronized String call(Function fn, Object[] args) {
        return (String) Context.call(null, fn, scope, scope, args);
    }
    
    
    private void parseLessException(Exception root) throws LessException {
        logger.debug("Parsing LESS Exception", root);
        if (root instanceof JavaScriptException) {
            Scriptable value = (Scriptable) ((JavaScriptException) root).getValue();
            boolean hasName = ScriptableObject.hasProperty(value, "name");
            boolean hasType = ScriptableObject.hasProperty(value, "type");
            if (hasName || hasType) {
                String errorType = "Error";
                if (hasName) {
                    String type = (String) ScriptableObject.getProperty(value, "name");
                    if ("ParseError".equals(type)) {
                        errorType = "Parse Error";
                    } else {
                        errorType = type + " Error";
                    }
                } else if (hasType) {
                    Object prop = ScriptableObject.getProperty(value, "type");
                    if (prop instanceof String) {
                        errorType = (String) prop + " Error"; 
                    }
                }
                String message = (String) ScriptableObject.getProperty(value, "message");
                String filename = "";
                if (ScriptableObject.hasProperty(value, "filename")) {
                    filename = (String) ScriptableObject.getProperty(value, "filename"); 
                }
                int line = -1;
                if (ScriptableObject.hasProperty(value, "line")) {
                    line = ((Double) ScriptableObject.getProperty(value, "line")).intValue(); 
                }
                int column = -1;
                if (ScriptableObject.hasProperty(value, "column")) {
                    column = ((Double) ScriptableObject.getProperty(value, "column")).intValue();
                }               
                List<String> extractList = new ArrayList<String>();
                if (ScriptableObject.hasProperty(value, "extract")) {
                    NativeArray extract = (NativeArray) ScriptableObject.getProperty(value, "extract");
                    for (int i = 0; i < extract.getLength(); i++) {
                        if (extract.get(i, extract) instanceof String) {
                            extractList.add(((String) extract.get(i, extract)).replace("\t", " "));
                        }
                    }
                }
                throw new LessException(message, errorType, filename, line, column, extractList);
            }
        }
        throw new LessException(root);
    }    
}

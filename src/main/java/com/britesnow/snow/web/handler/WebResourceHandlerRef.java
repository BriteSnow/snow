package com.britesnow.snow.web.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.britesnow.snow.util.FileUtil;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.handler.annotation.WebResourceHandler;
import com.britesnow.snow.web.param.WebParamRef;
import com.britesnow.snow.web.param.WebParameterParser;

public class WebResourceHandlerRef extends BaseWebHandlerRef implements PathMatcher {

    private WebResourceHandler webResourceHandler;

    public WebResourceHandlerRef(Object object, Method method, Map<Class<? extends Annotation>,WebParameterParser> webParameterParserMap,
                             WebResourceHandler webFile) {
        super(object, method, webParameterParserMap);
        this.webResourceHandler = webFile;
        initWebParamRefs();
    }

    @Override
    public boolean matchesPath(String path) {
        String[] fileNameAndExt = FileUtil.getFileNameAndExtension(path);
        boolean match = false;
        //first match the ext.
        for (String ext : webResourceHandler.ext()) {
            
            if (ext.equalsIgnoreCase(fileNameAndExt[1])) {
                match = true;
                break;
            }
        }

        //if the match match, then, match the matches
        if (match) {
            for (String regex : webResourceHandler.matches()) {
                Pattern pat = Pattern.compile(regex);
                Matcher mat = pat.matcher(path);
                Boolean matches = mat.matches();
                if (matches) {
                    match = true;
                    break;
                }else{
                    match = false;
                }
            }
        }

        return match;
    }

    public Object invokeWebFile(RequestContext rc) throws Exception {
        Object[] paramValues = new Object[webArgRefs.size()];
        int i = 0;
        for (WebParamRef webParamRef : webArgRefs) {
            paramValues[i++] = webParamRef.getValue(method, rc);
        }
        return method.invoke(webHandler, paramValues);
    }

}

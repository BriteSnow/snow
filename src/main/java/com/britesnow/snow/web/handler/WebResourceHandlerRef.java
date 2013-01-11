package com.britesnow.snow.web.handler;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.britesnow.snow.util.FileUtil;
import com.britesnow.snow.web.handler.annotation.WebResourceHandler;

public class WebResourceHandlerRef extends WebHandlerRef implements PathMatcher {

    private WebResourceHandler webResourceHandler;

    public WebResourceHandlerRef(Class webClass, Method method, ParamDef[] paramDefs,
                            WebResourceHandler webResourceHandler) {
        super(webClass, method, paramDefs);
        this.webResourceHandler = webResourceHandler;
    }

    @Override
    public boolean matchesPath(String path) {
        String[] fileNameAndExt = FileUtil.getFileNameAndExtension(path);
        boolean extFilterOk = false;
        // first match the ext.
        // if we have a "ext" filter, then we pre-filter the path
        String[] exts = webResourceHandler.ext();
        if (exts.length > 0) {
            for (String ext : exts) {
                ext = ext.toLowerCase();
                String pathExt = fileNameAndExt[1].toLowerCase();
                if (ext.equalsIgnoreCase(pathExt)) {
                    extFilterOk = true;
                    break;
                }
            }
        } else {
            extFilterOk = true;
        }

        boolean match = false;
        // if the match match, then, match the matches
        if (extFilterOk) {
            for (String regex : webResourceHandler.matches()) {
                
                Pattern pat = Pattern.compile(regex);
                Matcher mat = pat.matcher(path);
                Boolean matches = mat.matches();
                if (matches) {
                    match = true;
                    break;
                } else {
                    match = false;
                }
            }
        }

        return match;
    }

}

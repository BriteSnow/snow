package com.britesnow.snow.web.renderer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;


import com.britesnow.snow.util.FileUtil;
import com.britesnow.snow.web.binding.WebAppFolder;
import com.google.inject.Singleton;

@Singleton
public class WebBundleManager {
    
    public static final String  WEB_BUNDLE_ALL_PREFIX         = "_web_bundle_all";
    
    @Inject
    private @WebAppFolder File webAppFolder;
    
    
    public boolean isWebBundle(String resourcePath){
        boolean r = false;

        String[] priPathAndExt = FileUtil.getFileNameAndExtension(resourcePath);

        if (priPathAndExt[0].endsWith(WEB_BUNDLE_ALL_PREFIX) && (priPathAndExt[1].equalsIgnoreCase(".js") || priPathAndExt[1].equalsIgnoreCase(".css"))) {
            r = true;
        }
        return r;        
    }
    
    public String getContent(String resourcePath){
        
        String ext = FileUtil.getFileNameAndExtension(resourcePath)[1];
        File resourceFolder = new File(webAppFolder,resourcePath).getParentFile();
        
        List<File> files = getWebBundleFiles(resourceFolder,ext);
        
        StringBuilder contentSB = new StringBuilder();
        for (File file : files) {
            contentSB.append(FileUtil.getFileContentAsString(file));
            contentSB.append("\n");
        }
        
        String content = contentSB.toString();
        return content;        
        
    }
    
    
    public List<File> getWebBundleFiles(File folder, String fileExt){
        List<File> files = null;
        File[] allFiles = FileUtil.getFiles(folder, fileExt);

        //TODO: look at the "all.bundle" file for custom inclusion/ordering
        files = Arrays.asList(allFiles);

        return files;        
    }    
}

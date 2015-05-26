package com.britesnow.snow.web.path;

import java.util.ArrayList;
import java.util.List;

import com.britesnow.snow.web.RequestContext;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class DefaultFramePathsResolver implements FramePathsResolver {
    static final private String   FRAME_NAME          = "_frame";

    @Inject
    private ResourceFileLocator      pathFileResolver;

    /**
     * By default, return the framPath for the resourcePath.
     */
    @Override
    public String[] resolve(RequestContext rc) {
        String path = rc.getResourcePath();
        return resolveResourcePath(path, rc);
    }

    /**
     * <p>Build the FramePath array from a path (e.g., "/blog/html5/my-html5-architecture").</p>
     * <p>Note that RequestContext hee is just for the pathFileResolve that requires it (not used for the path, as it is given as parameter)</p>
     *
     * @param path
     * @return
     */
    public String[] resolveResourcePath(String path, RequestContext rc){
        String framePath;

        List<String> framePaths = new ArrayList<String>();

        List<String> pathArray = new ArrayList<String>();
        Iterables.addAll(pathArray, Splitter.on("/").split(path));
        for (int i = pathArray.size() -1; i > 0; i--){
            framePath = Joiner.on("/").join(pathArray.subList(0, i));
            framePath += "/" + FRAME_NAME;
            if (!framePath.startsWith("/")){
                framePath = "/" + framePath;
            }

            // TODO: need to change this harcoded ".ftl" when we support multi template types
            if (pathFileResolver.locate(framePath + ".ftl",rc).exists()){
                framePaths.add(framePath);
            }
        }

        framePaths = Lists.reverse(framePaths);

        return framePaths.toArray(new String[framePaths.size()]);
    }


}
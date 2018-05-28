package com.britesnow.snow.web.rest;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import com.britesnow.snow.util.Pair;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.HttpMethod;
import com.britesnow.snow.web.handler.ParamDef;

/**
 * Note: the registerWeb*** must be called only in init time, as the Map are not concurrent 
 * for performance reasons. 
 * 
 * Internal only. 
 * 
 * @author jeremychone
 * @since  2.0.0
 *
 */
@Singleton
public class RestRegistry {
    
    private Map<String,WebRestRef> webGetRefByPath = new HashMap<String, WebRestRef>();
    private Map<String,WebRestRef> webPostRefByPath = new HashMap<String, WebRestRef>();
    private Map<String,WebRestRef> webPutRefByPath = new HashMap<String, WebRestRef>();
	private Map<String,WebRestRef> webPatchRefByPath = new HashMap<String, WebRestRef>();
    private Map<String,WebRestRef> webDeleteRefByPath = new HashMap<String, WebRestRef>();
	private Map<String,WebRestRef> webOptionsRefByPath = new HashMap<String, WebRestRef>();
    
    private List<WebRestRef> patternWebGetRefList = new ArrayList<WebRestRef>();
    private List<WebRestRef> patternWebPostRefList = new ArrayList<WebRestRef>();
    private List<WebRestRef> patternWebPutRefList = new ArrayList<WebRestRef>();
	private List<WebRestRef> patternWebPatchRefList = new ArrayList<WebRestRef>();
    private List<WebRestRef> patternWebDeleteRefList = new ArrayList<WebRestRef>();
	private List<WebRestRef> patternWebOptionsRefList = new ArrayList<WebRestRef>();
    
    // --------- WebRef Getters --------- //
    public WebRestRef getWebRestRef(RequestContext rc){
        return getWebRestRef(rc,null);
    }
    
    public WebRestRef getWebRestRef(RequestContext rc,String resourcePath){
        if (resourcePath == null){
            resourcePath = rc.getResourcePath();
        }
        HttpMethod method = rc.getMethod();
        
        WebRestRef ref = null;
        // first, check if there is a perfect match. 
        Map<String,WebRestRef> refByPath = getRefByPath(method);
        if (refByPath != null){
            ref = refByPath.get(resourcePath);
        }
        
        // if still null, check if there is a vared path matching
        if (ref == null){
            List<WebRestRef> patterRefList = getPatternWebRestRef(method);
            if(patterRefList != null){
                for (WebRestRef patternWebRestRef : patterRefList){
                    Matcher matcher = patternWebRestRef.getPathPattern().matcher(resourcePath);
					boolean isMatching = matcher.matches();
                    if (isMatching){
                        ref = patternWebRestRef;
                        break;
                    }
                }
            }
        }
        
        return ref;
    }
    // --------- /WebRef Getters --------- //
    
    
    // --------- Register Methods (called in init only) --------- //
    public void registerWebRest(Class webClass, Method m, ParamDef[] paramDefs, HttpMethod method, String[] paths ){
        Map<String,WebRestRef> refByPath = getRefByPath(method);
        List<WebRestRef> patternWebRestRefList = getPatternWebRestRef(method);
        
        if (refByPath != null){
            WebRestRef ref = new WebRestRef(webClass,m, paramDefs);
            // if now paths, in the .value() then take the methodName as the path
            if (paths.length == 0){
                refByPath.put("/" + ref.getHandlerMethod().getName(), ref);
            }else{
                for (String path : paths){
                    if (path.indexOf("{") > -1){
                        Pair<Pattern, Map<Integer, String>> patternAndMap = getPathPatternAndMap(path);
                        Pattern pattern = patternAndMap.getFirst();
                        WebRestRef varPathRef = new WebRestRef(webClass,m, paramDefs,path, pattern,patternAndMap.getSecond());
						patternWebRestRefList.add(varPathRef);
                    }else{
                        refByPath.put(path, ref);
                    }
                }
            }
        }
    }

    /** To be called once, after all is registered to order and do any registry optimization needed
     *  TODO: Ideally this shoul dnot be public, but because it needs to be access from web.handler.WebObjectRegistry, it has for now
     *  TODO: Also, we need to mmake this "more" thread safe.
     *  */
    public void finalize(){
    	// TODO: In future release we need to make sure this is thread safe. In practice registerWebRest and
		//       .finilize are not called but since they have to be public (to be accessilble from WebObjectRegistry, they theorically could)
		VaredPathComparator comparator = new VaredPathComparator();
		Collections.sort(patternWebGetRefList,comparator);
		for (WebRestRef w : patternWebGetRefList){
			System.out.println(w.getPath());
		}
		Collections.sort(patternWebPostRefList,comparator);
		Collections.sort(patternWebPutRefList,comparator);
		Collections.sort(patternWebDeleteRefList,comparator);
    }
    
    
    // --------- /Register Methods (called in init only) --------- //
    private Pair<Pattern,Map<Integer,String>> getPathPatternAndMap(String path){
        
        // --- find the var names --- //
        final String RGX_FIND_VAR =  "\\{(.*?)\\}";
        
        Pattern varFinderPattern = Pattern.compile(RGX_FIND_VAR);
        Matcher varFinderMatcher = varFinderPattern.matcher(path);
        
        Map<Integer,String> paramNameByIdx = new HashMap<Integer, String>();
        
        int group_idx = 0;
        StringBuilder pathRegExSb = new StringBuilder("^");
        String str = path;
        int str_idx = 0;
        while (varFinderMatcher.find()) {
            String paramName = varFinderMatcher.group();
            paramName = paramName.substring(1,paramName.length() -1);
            paramNameByIdx.put(group_idx, paramName);
            pathRegExSb.append(regexEscape(str.substring(str_idx, varFinderMatcher.start())));
            pathRegExSb.append("(.*?)");
            str_idx = varFinderMatcher.end();
            group_idx++;
        }
        pathRegExSb.append(regexEscape(str.substring(str_idx)));
        pathRegExSb.append("$");
        
        String pathRegEx = pathRegExSb.toString();
        Pattern pathPattern = Pattern.compile(pathRegEx);
        
        return new Pair<Pattern,Map<Integer,String>>(pathPattern,paramNameByIdx);
    }
    
    static private String regexEscape(String str){
        return str.replace(".", "\\.");
    }    
    
    private List<WebRestRef> getPatternWebRestRef(HttpMethod httpMethod){
        switch (httpMethod){
            case GET:
                return patternWebGetRefList;
            case POST: 
                return patternWebPostRefList;
            case PUT:
                return patternWebPutRefList;
			case PATCH:
				return patternWebPatchRefList;
            case DELETE:
                return patternWebDeleteRefList;
			case OPTIONS:
				return patternWebOptionsRefList;
            default:
                return null;
        }        
    }
    
    private Map<String,WebRestRef> getRefByPath(HttpMethod httpMethod){

        switch (httpMethod){
            case GET: 
                return webGetRefByPath;
            case POST: 
                return webPostRefByPath;
            case PUT:
                return webPutRefByPath;
			case PATCH:
				return webPatchRefByPath;
            case DELETE:
                return webDeleteRefByPath;
			case OPTIONS:
				return webOptionsRefByPath;
            default: 
                return null;
        }        
    }
    


    
}

class VaredPathComparator implements Comparator<WebRestRef>{

	@Override
	public int compare(WebRestRef wref1, WebRestRef wref2) {
		String fix1 = removeAllVars(wref1.getPath());
		String fix2 = removeAllVars(wref2.getPath());
		if (fix1.length() > fix2.length()){
			return -1; // wref1 takes pecedence (smaller, will get earlier in the array)
		}else if (fix1.length() < fix2.length()){
			return 1;
		}else{
			return fix1.compareTo(fix2);
		}
	}


	/** Remove all eventual {} so that we can know the length of fix things */
	static private String removeAllVars(String s){
		Pattern p = Pattern.compile("(\\{.*?\\})",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		String sCollapsed =  p.matcher(s).replaceAll("");
		return sCollapsed;
	}


}

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
		String o1 = collapse(wref1.getPath());
		String o2 = collapse(wref2.getPath());

		Integer[] o1Indexes = allIndexOf(o1, '{');
		Integer[] o2Indexes = allIndexOf(o2, '{');


		// if none has path var, then, order alphabitically
		if (o1Indexes.length == 0 && o2Indexes.length == 0){
			return o1.compareTo(o2);
		}

		// if we reach here, it means that one of them have a at least {

		// if one them have no {, then, it wins (smaller)
		if (o1Indexes.length == 0 && o2Indexes.length > 0){
			return -1; // o1 is ealier (smaller than o2)
		}
		if (o1Indexes.length > 0 && o2Indexes.length == 0){
			return 1; // o1 is later (greater than o2)
		}

		// if we are here, it means they both have some {
		int min = Math.min(o1Indexes.length, o2Indexes.length);

		for (int i = 0; i < min; i++){
			if (o1Indexes[i] > o2Indexes[i]){
				return -1; // o1 wins, smaller
			}
			if (o1Indexes[i] < o2Indexes[i]){
				return 1; // o1 lose, greater than 02
			}
		}

		// if we are here, this means that all {} position match the ones that have the least.

		// If one has more {} than the other, then, it wins
		if (o1Indexes.length != o2Indexes.length){
			return (o1Indexes.length != o2Indexes.length) ? -1 : 1;
		}

		// if we are here, it means that thye both have the same number of {} and then, the longest win
		if (o1.length() != o2.length()){
			return (o1.length() > o2.length()) ? -1 : 1;
		}

		// if we are here all was the same, so, we just order alphabitically.
		return o1.compareTo(o2);
	}

	static private Integer[] allIndexOf(String str, char c){
		List<Integer> indexes = new ArrayList<Integer>();

		int idx = 0;
		for (char cs : str.toCharArray()){
			if (cs == c){
				indexes.add(idx);
			}
			idx ++;
		}

		return indexes.toArray(new Integer[indexes.size()]);
	}

	/** Collapse the varPath {...} to {} so that the length of the name does not take in consideration */
	static private String collapse(String s){
		Pattern p = Pattern.compile("\\{(.*?)\\}",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		String sCollapsed =  p.matcher(s).replaceAll("{}");
		return sCollapsed;
	}
}

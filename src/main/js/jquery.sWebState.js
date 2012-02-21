
/**
 * set jQuery.sWebState to this function if you want each parameter per context
 * to have its own cookie.
 * @param stateContext
 * @param name
 * @param value
 */
jQuery.sWebStateMultiCookie = function(stateContext,name,value){
	var cookieName = stateContext + "." + name;
	if (typeof value == 'undefined' ){
		return $.cookie(cookieName);
	}
    else if ($.isFunction(value)) {
        $.cookie(cookieName, value($.cookie(cookieName)));
    } else {
		$.cookie(cookieName,value);
	}
};

/**
 * set jQuery.sWebState to this function if you want each context to have
 * a single cookie consisting of a url-encoded parameter string.
 * @param stateContext
 * @param name
 * @param value
 */
jQuery.sWebStateSingleCookie = function(stateContext,name,value){

  var parameters = {};
  var currentVal = $.cookie(stateContext);

  if(currentVal) {
    $.each(currentVal.split('&'), function(key, value) {
      var paramArr = value.split('=', 2);
      if(paramArr.length == 2) {
        parameters[decodeURIComponent(paramArr[0])] = decodeURIComponent(paramArr[1]);
      }
    });
  }

  if (typeof value == 'undefined' ){
    return parameters[name];
  }
  else {

    parameters[name] = $.isFunction(value) ? value(parameters[name]) : value;

    var cookieVal = '';
    $.each(parameters, function(key, value) {
      if(cookieVal.length > 0) {
        cookieVal += '&';
      }
      cookieVal += encodeURIComponent(key) + '=' + (value ? encodeURIComponent(value) : '');
    });

    $.cookie(stateContext, cookieVal);
  }
};



/**
 * Usage:
 *
 * //Set the data
 * $("#table_id").sWebState("sortColumn","name");
 * $("#table_id").sWebState("sortOrder","asc");
 * $.sWebState("table_id","sortColumn","name");
 *
 * //Get the ui state
 * var sortOrder = $("#table_id").sWebState("sortOrder");
 *
 * @param {Object} stateContext
 * @param {Object} name
 * @param {Object} value (Optional) get if not present
 */
jQuery.sWebState = jQuery.sWebStateSingleCookie;




jQuery.sWebStateSession = function(stateContext,name,value){
    /* if somebody want change the 'akui_' to any other prefix then change the AKUI_COOKIE_PREFIX in CookieAuthenticatedUserTracker */
    /* CHECK: Simply make it a sesion cookie instead of cleaning ourselves? */
    var AKUI_COOKIE_PREFIX = "akui_";
	var cookieName = stateContext + "." + name;
	if (typeof value == 'undefined' ){
		return $.cookie(AKUI_COOKIE_PREFIX + cookieName);
	}else{
		$.cookie(AKUI_COOKIE_PREFIX + cookieName,value);
	}
};

(function($) {

	/**
	 * Set or get the WebState for id of an Element.
	 *  
	 * Note that for the get, it will return the first match state. 
	 * 
	 * @param {Object} name (required): Name of the state
	 * @param {Object} value (optional) : Value to put in the WebState
	 */
    $.fn.sWebState = function(name, value){
        if (typeof value == 'undefined') {
			var stateContext = this.attr("id");
        	return $.sWebState(stateContext, name);
        } 
		else {
            // iterate and process each matched element
            return this.each(function(){
                var $this = $(this);
                var stateContext = $this.attr("id");
                $.sWebState(stateContext, name, value);
            });
        }
        
    };


})(jQuery);


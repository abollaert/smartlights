
var apiBase = "http://127.0.0.1:8080";

/**
 * Returns details for all the modules.
 */
function apiLoadModules(successHandler, errorHandler) {
	doVoidAjaxCall(successHandler, errorHandler, "/api/GetModules");
}

function doVoidAjaxCall(successHandler, errorHandler, apiURL) {
	$.post(apiBase + apiURL, { format : "json", callback : "?" }, successHandler);
}
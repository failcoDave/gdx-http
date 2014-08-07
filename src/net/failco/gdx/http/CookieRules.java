/**
 * 
 */

package net.failco.gdx.http;

import java.net.URI;

/** Helper class for cookie-related checks
 * @author David Hull */
class CookieRules {
	/** Checks to see if given URI is eligible to be sent given cookie
	 * @param cookie
	 * @param uri
	 * @return */
	public boolean isCookieValid (Cookie cookie, URI uri) {
		if (!uri.getHost().contains(cookie.domain)) return false;
		if (!isPathValid(cookie, uri)) return false;
		if (cookie.secure && !uri.getScheme().equalsIgnoreCase("https"))
			return false;
		
		return true;

	}

	private boolean isPathValid (Cookie cookie, URI uri) {
		// If the URI path is null, make sure no path is specified in the cookie, or the path is '/'
		final String uriPath = uri.getPath();
		if (uriPath == null || uriPath.equals("/")) {
			return (cookie.path == null || cookie.path.equals("/"));
		}
		return uriPath.contains(cookie.path);

	}

}

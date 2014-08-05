/**
 * 
 */

package net.failco.gdx.http;

import java.net.URI;

/** Helper class to parse cookies as supplied by HTTP response headers
 * @author David Hull */
class CookieParser {
	public Cookie createCookieByHeader (String cookieHeader, URI sourceUri) {

		// Cookie cookie = Pools.obtain(Cookie.class);
		String[] keyValPairs = cookieHeader.split("; ?");
		// first cookie key-val pair is always name=value
		String keyVal[] = keyValPairs[0].split("=");
		Cookie cookie = Cookie.obtain(keyVal[0], keyVal[1]);

		// after name=value, grab and check other key-value attributes
		for (int i = 1; i < keyValPairs.length; i++) {
			keyVal = keyValPairs[i].split("=");
			/*
			 * the "secure" property doesn't have an "= true" part, so check for that. There's not any point to catch HttpOnly,
			 * otherwise that check would go here too
			 */
			if (keyVal.length < 2) {
				if ("Secure".equals(keyVal[0])) {
					cookie.secure = true;
				}
				continue;
			}

			parseAndSetAttribute(cookie, keyVal);

		}

		if (cookie.domain == null) {
			cookie.domain = sourceUri.getHost();
		}
		if (cookie.path == null) {
			cookie.path = sourceUri.getPath();

		}
		return cookie;

	}

	private void parseAndSetAttribute (Cookie cookie, String[] keyVal) {
		// silently ignore malformed or unrecognized attributes
		if (keyVal.length < 2) {
			return;
		}
		// can't switch on Strings in 1.6
		final String key = keyVal[0];

		if ("expires".equalsIgnoreCase(key)) {
			cookie.setExpiration(keyVal[1]);
		} else if ("domain".equalsIgnoreCase(key)) {
			cookie.domain = (keyVal[1].length() > 0) ? keyVal[1] : null;
		} else if ("path".equalsIgnoreCase(key)) {
			cookie.path = (keyVal[1].length() > 0) ? keyVal[1] : null;
		}

	}

}

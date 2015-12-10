package net.failco.gdx.http;

import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponseListener;

/**
 * An executor for GDX {@link HttpRequest}s. Allows clients to set http headers to be automatically
 * attached to outgoing {@Link HttpRequest}s.  Intercepts responses and checks for cookies, which, if appropriate,
 * will be attached to the next request.
 *
 * See also {@link CookieManager}
 *
 * @author David Hull */
public interface HttpClient {

	/** Convenience class for some common HTTP Header field names. */
	class HeaderFields {
		public static final String COOKIE = "Cookie";
		public static final String USER_AGENT = "User-Agent";

		public static final String ACCEPT = "Accept";
		public static final String ACCEPT_CHARSET = "Accept-Charset";

		public static final String CONTENT_TYPE = "Content-Type";
		public static final String DATE = "Date";
	}

	void sendHttpRequest(HttpRequest httpRequest, HttpResponseListener httpResponseListener);

	/**
	 * Sets the value for the given field, which will be sent on every subsequent request.
	 *
	 * @param field case-sensitive. If field was used previously, old record will be replaced with this one.
	 * @param value If null, the given field will no longer be sent
	 */
	void setHeader(String field, String value);

	/**
	 * @param field
	 * @return value of the given field, or null if header field hasn't been set
	 */
	String getHeader(String field);

	/**
	 * @return may be null
	 */
	CookieManager getCookieManager();

	/**
	 * @param cookieMgr may be null
	 */
	void setCookieManager(CookieManager cookieMgr);

}

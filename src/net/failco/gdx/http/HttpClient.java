/**
 * 
 */

package net.failco.gdx.http;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;


/** TODO WRITE ME!!!
 * 
 * Does NOT support multi-threading
 * 
 * @author David Hull */
public class HttpClient {

	private final ObjectMap<String, String> headerFields = new ObjectMap<String, String>();

	CookieManager cookieMgr;

	public void sendHttpRequest (HttpRequest httpRequest, HttpResponseListener httpResponseListener) {

		if (cookieMgr != null && !cookieMgr.isEmpty()) {
			String tempCookie = cookieMgr.getHeaderPayload();
			httpRequest.setHeader(HeaderFields.COOKIE, tempCookie);
			Gdx.app.log("Outgoing request cookie:", tempCookie);
		}
		if (headerFields.size > 0) {
			for (Entry<String, String> entry : headerFields.entries()) {
				httpRequest.setHeader(entry.key, entry.value);

			}
		}
		// Listener instantiation below...
		Gdx.net.sendHttpRequest(httpRequest, new ResponseWrapper(httpResponseListener));
	}

	/** Sets the value for the given field, which will be sent on every subsequent request.
	 * @param field case-sensitive.  If field was used previously, old record will be replaced with this one.
	 * @param value If null, the given field will no longer be sent */
	public void setHeaderField (String field, String value) {
		if (value == null)
			headerFields.remove(field);
		else
			headerFields.put(field, value);
	}

	/** @param field
	 * @return value of the given field, or null if header field hasn't been set */
	public String getHeaderField (String field) {
		return headerFields.get(headerFields.get(field), null);
	}

	/** @return may be null */
	public CookieManager getCookieMgr () {
		return cookieMgr;
	}

	/** @param cookieMgr may be null */
	public void setCookieMgr (CookieManager cookieMgr) {
		this.cookieMgr = cookieMgr;
	}

	private final class ResponseWrapper implements HttpResponseListener {
		private final HttpResponseListener listener;

		public ResponseWrapper (HttpResponseListener listener) {
			this.listener = listener;
		}

		@Override
		public void handleHttpResponse (HttpResponse httpResponse) {

			if (cookieMgr != null) {
				Map<String, List<String>> list = httpResponse.getHeaders();
				List<String> cookieStrings = list.get("Set-Cookie");
				for (String cookie : cookieStrings) {
					cookieMgr.registerSetCookieHeader(cookie);
				}
				cookieMgr.save();
			}

			listener.handleHttpResponse(httpResponse);

		}

		@Override
		public void failed (Throwable t) {
			listener.failed(t);

		}

		@Override
		public void cancelled () {
			listener.cancelled();
			
		}
	};

	/** Convenience class for some common HTTP Header field names
	 * @author David Hull */
	public static class HeaderFields {
		public static final String COOKIE = "Cookie";
		public static final String USER_AGENT = "User-Agent";

	}

}

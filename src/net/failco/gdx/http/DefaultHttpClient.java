/**
 * 
 */

package net.failco.gdx.http;

import java.net.URI;
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
public class DefaultHttpClient implements HttpClient {

	private final ObjectMap<String, String> headerFields = new ObjectMap<String, String>();

	/**
	 * May be null
	 */
	CookieManager cookieMgr;

	public DefaultHttpClient () {
		this.cookieMgr = new CookieManager();
		this.cookieMgr.load();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.failco.gdx.http.HttpClient#sendHttpRequest(com.badlogic.gdx.Net.HttpRequest,
	 * com.badlogic.gdx.Net.HttpResponseListener)
	 */
	@Override
	public void sendHttpRequest (HttpRequest httpRequest, HttpResponseListener httpResponseListener) {

		URI uri = URI.create(httpRequest.getUrl());

		if (cookieMgr != null && !cookieMgr.isEmpty()) {
			String tempCookie = cookieMgr.getHeaderPayload(uri);
			httpRequest.setHeader(HttpClient.HeaderFields.COOKIE, tempCookie);
			
		}
		if (headerFields.size > 0) {
			for (Entry<String, String> entry : headerFields.entries()) {
				httpRequest.setHeader(entry.key, entry.value);

			}
		}
		// Listener instantiation below...
		Gdx.net.sendHttpRequest(httpRequest, new ResponseWrapper(httpResponseListener, uri));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.failco.gdx.http.HttpClient#setHeaderField(java.lang.String, java.lang.String)
	 */
	@Override
	public void setHeader (String field, String value) {
		if (value == null)
			headerFields.remove(field);
		else
			headerFields.put(field, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.failco.gdx.http.HttpClient#getHeader(java.lang.String)
	 */
	@Override
	public String getHeader (String field) {
		return headerFields.get(headerFields.get(field), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.failco.gdx.http.HttpClient#getCookieMgr()
	 */
	@Override
	public CookieManager getCookieManager () {
		return cookieMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.failco.gdx.http.HttpClient#setCookieMgr(net.failco.gdx.http.CookieManager)
	 */
	@Override
	public void setCookieManager (CookieManager cookieMgr) {
		this.cookieMgr = cookieMgr;
	}

	private final class ResponseWrapper implements HttpResponseListener {
		private final HttpResponseListener listener;
		private final URI sourceUri;

		public ResponseWrapper (HttpResponseListener listener, URI targetURI) {
			this.listener = listener;
			this.sourceUri = targetURI;
		}

		@Override
		public void handleHttpResponse (HttpResponse httpResponse) {

			if (cookieMgr != null) {				
				List<String> cookieStrings = httpResponse.getHeaders().get("Set-Cookie");
				if (cookieStrings != null) {
					for (String cookie : cookieStrings) {
						cookieMgr.registerSetCookieHeader(cookie, sourceUri);
					}
					cookieMgr.save();
				}
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
	}

}

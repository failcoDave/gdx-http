
package net.failco.gdx.http;

import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.StringBuilder;

/** 
 * Handles the storage and management of cookies.
 * @author David Hull */
public class CookieManager {

	private static final String SET_COOKIE_REGEX_SPLITTER = ";  ?";
	private static final String seperator = "; ";
	private static final String PREF_FILE_NAME = "net.failco.gdx.http.prefs";
	private static final String COOKIE_ENTRY_PREF_KEY = "cookies";

	private final StringBuilder stringer = new StringBuilder(64);

	private final Array<Cookie> cookies = new Array<Cookie>();

	private final CookieParser parser = new CookieParser();
	private final CookieRules rules = new CookieRules();

	/** Scratch {@link Date} for comparing expiration dates. */
	private final Date now = new Date();

	private final Json json = new Json();
	private final JsonReader jsonReader = new JsonReader();
	
	
	

	/** Loads cookies from Preferences */
	public void load () {
		// for expiration checks
		now.setTime(TimeUtils.millis());

		Preferences prefs = Gdx.app.getPreferences(PREF_FILE_NAME);
		final String storedString = prefs.getString(COOKIE_ENTRY_PREF_KEY, null);
		if (storedString != null) {
			JsonValue jsonVal = jsonReader.parse(storedString);
			for (JsonValue entry = jsonVal.child; entry != null; entry = entry.next) {
				Cookie cookie = json.readValue(Cookie.class, entry);
				/*
				 * if expiry is null, it shouldn't have been saved in the first place. If expired, ignore. It'll be overwritten in
				 * time
				 */

				if (cookie.expiration != null && cookie.expiration.after(now)) this.cookies.add(cookie);

			}
			

		}

	}

	/** Saves stored cookies to {@link Preferences}. */
	public void save () {
		Json json = new Json();
		StringWriter writer = new StringWriter();
		json.setWriter(new JsonWriter(writer));

		now.setTime(TimeUtils.millis());
		Iterator<Cookie> it = cookies.iterator();
		json.writeArrayStart();
		while (it.hasNext()) {
			Cookie cookie = it.next();
			// if cookie's expired, remove from cached list. If no expiration given, don't save but don't remove
			if (cookie.expiration != null) {
				if (cookie.expiration.before(now)) {
					it.remove();
				} else {
					json.writeValue(cookie);
				}

			}

		}
		json.writeArrayEnd();
		Preferences prefs = Gdx.app.getPreferences(PREF_FILE_NAME);
		prefs.putString(COOKIE_ENTRY_PREF_KEY, writer.toString());
		prefs.flush();

	}

	/** @param key case-sensitive
	 * @return may be null */
	public String getCookieValue (final String key) {
		Cookie cookie = getCookie(key);
		return (cookie == null) ? null : cookie.value;

	}

	/** @param key case-sensitive
	 * @return the reference to the cookie stored with the given name. May be null */
	public Cookie getCookie (final String key) {
		if (key == null) throw new IllegalArgumentException("Key cannot be null");
		for (Cookie cookie : cookies) {
			if (cookie.name.equals(key)) return cookie;
		}
		return null;
	}

	/** Builds the String formatted to be placed in the HTTP header when sending requests
	 * @param uri the URI that the HTTP request is being sent to
	 * 
	 * @return */
	public String getHeaderPayload (URI uri) {

		now.setTime(TimeUtils.millis());
		stringer.setLength(0);
		Iterator<Cookie> it = cookies.iterator();

		while (it.hasNext()) {
			Cookie cookie = it.next();
			// while we're here, ignore and remove any expired cookies
			if (cookie.expiration != null && cookie.expiration.compareTo(now) < 0) {
				it.remove();
				continue;
			}
			if (rules.isCookieValid(cookie, uri)) {
				stringer.append(cookie.name).append('=').append(cookie.value);
			}
			if (it.hasNext()) {
				stringer.append(seperator);
			}

		}
		return stringer.toString();

	}

	/** resets cookie list */
	public void clear () {
		for (Cookie cookie : cookies) {
			Cookie.free(cookie);
		}
		cookies.clear();

	}

	/** @return true if no cookie keys have been specified, false otherwise */
	public boolean isEmpty () {
		return (cookies.size == 0);
	}

	/** @param headerCookiePayload the cookie as supplied by the HTTP Set-Cookie header
	 * @param sourceUri uri */
	protected void registerSetCookieHeader (String headerCookiePayload, URI sourceUri) {		
		Cookie cookie = parser.createCookieByHeader(headerCookiePayload, sourceUri);
		if (cookie != null) {
			addCookie(cookie);
		}

	}

	public void addCookie (Cookie cookie) {
		if (cookie == null) throw new IllegalArgumentException("cookie cannot be null");
		// if a cookie with the given name already exists, replace it with the
		// new one
		for (int i = 0; i < cookies.size; i++) {
			Cookie old = cookies.get(i);
			// note: cookie names are case-sensitive
			if (old.name.equals(cookie.name)) {
				cookies.set(i, cookie);

				return;
			}

		}
		cookies.add(cookie);

	}

	@Override
	public String toString () {
		stringer.setLength(0);
		stringer.append("CookieManager: ").append(cookies.toString());
		return stringer.toString();

	}

}

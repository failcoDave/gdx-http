/**
 * 
 */

package net.failco.gdx.http;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;

/** Represents a single cookie, including the name-value pair, specified domain, and expiration date.
 * 
 * @author David Hull */
public class Cookie implements Pool.Poolable, Json.Serializable {
	private static final DateFormat cookieDateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss z");

	public String name;
	public String value;
	public String domain;
	/** Expiration date of the cookie, as given by the server. After the cookie expires it will not be sent with future requests and
	 * will eventually be removed from the {@link CookieManager}. If null, the cookie expires when the CookieManager is disposed,
	 * meaning it will not be serialized. This mirrors browser behavior when cookie expirations are not set. */
	public Date expiration;

	/** Enforce using factory methods */
	private Cookie () {

	}

	/** Attempts to parse and assign the given date as supplied by the incoming cookie. If the string cannot be parsed, expiration
	 * is left as null.
	 * @param dateString
	 * @return true if given date was successfully parsed and assigned, false if there was an error. */
	public boolean setExpiration (String dateString) {
		try {
			expiration = cookieDateFormat.parse(dateString);
			return true;

		} catch (ParseException e) {
			Gdx.app.error("Cookie", "Cannot parse expiration date: " + dateString);
			return false;
		}

	}

	private void parseAndSetAttribute (String[] keyVal) {
		// silently ignore malformed or unrecognized attributes
		if (keyVal.length < 2) {
			return;
		}
		// can't switch on Strings in 1.6
		final String key = keyVal[0];

		if ("expires".equalsIgnoreCase(key)) {
			setExpiration(keyVal[1]);
		} else if ("domain".equalsIgnoreCase(key)) {
			domain = (keyVal[1].length() > 0) ? keyVal[1] : null;
		}

	}

	@Override
	public String toString () {
		String ret = name + "=" + value;
		if (expiration != null) {
			ret += "; " + "expires=" + cookieDateFormat.format(expiration);
		}
		if (domain != null) {
			ret += "; " + "domain=" + domain;
		}
		return ret;
	}

	public static Cookie parse (final String cookieHeader) {
		// Cookie cookie = Pools.obtain(Cookie.class);
		String[] keyValPairs = cookieHeader.split("; ?");
		// first cookie key-val pair is always name=value
		String keyVal[] = keyValPairs[0].split("=");
		Cookie cookie = Cookie.obtain(keyVal[0], keyVal[1]);

		// after name=value, grab and check other key-value attributes
		for (int i = 1; i < keyValPairs.length; i++) {
			keyVal = keyValPairs[i].split("=");
			if (keyVal.length < 2) {
				continue;
			}

			cookie.parseAndSetAttribute(keyVal);

		}
		return cookie;
	}

	/** Obtains a cookie instance from the pool, assigning the given cookie name and value
	 * @param name
	 * @param value
	 * @return */
	public static Cookie obtain (final String name, final String value) {
		Cookie ret = pool.obtain();
		ret.name = name;
		ret.value = value;
		return ret;

	}

	@Override
	public void reset () {
		name = null;
		value = null;
		domain = null;
		expiration = null;

	}

	public static void free (Cookie cookie) {
		pool.free(cookie);

	}

	private static final Pool<Cookie> pool = new Pool<Cookie>() {

		@Override
		protected Cookie newObject () {
			return new Cookie();
		}

	};

	@Override
	public void write (Json json) {

		json.writeValue("name", name);
		json.writeValue("value", value);
		if (domain != null) json.writeValue("domain", domain);
		if (expiration != null) json.writeValue("expiration", expiration.getTime());

	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		// name and value are both required
		this.name = jsonData.getString("name");
		this.value = jsonData.getString("value");

		this.domain = jsonData.getString("domain", null);

		long dateTimestamp = jsonData.getLong("expiration", -1);
		if (dateTimestamp > 0) {

			this.expiration = new Date(dateTimestamp);

		} else {
			this.expiration = null;
		}

	}

	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((expiration == null) ? 0 : expiration.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Cookie other = (Cookie)obj;
		if (domain == null) {
			if (other.domain != null) {
				return false;
			}
		} else if (!domain.equals(other.domain)) {
			return false;
		}
		if (expiration == null) {
			if (other.expiration != null) {
				return false;
			}
		} else if (!expiration.equals(other.expiration)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

}

// Copyright (C) 1999-2001 by Jason Hunter <jhunter_AT_acm_DOT_org>.
// All rights reserved. Use of this class is limited.
// Please see the LICENSE for more information.

package net.simpleframework.lib.com.oreilly.servlet;

import java.util.Hashtable;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * A class to simplify cookie retrieval. It can retrieve cookie values by name
 * and return the value as any primitive type (no casting or parsing required).
 * It can also throw an exception when a cookie is not found (simplifying error
 * handling), and can accept default values (eliminating error handling).
 * <p>
 * It is used like this: <blockquote>
 * 
 * <pre>
 * CookieParser parser = new CookieParser(req);
 * 
 * float ratio = parser.getFloatCookie(&quot;ratio&quot;, 1.0);
 * 
 * int count = 0;
 * try {
 * 	count = parser.getIntCookie(&quot;count&quot;);
 * } catch (NumberFormatException e) {
 * 	handleMalformedCount();
 * } catch (CookieNotFoundException e) {
 * 	handleNoCount();
 * }
 * </pre>
 * 
 * </blockquote>
 * 
 * @see com.oreilly.servlet.CookieNotFoundException
 * 
 * @author <b>Jason Hunter</b>, Copyright &#169; 2000
 * @version 1.0, 2000/03/19
 */
public class CookieParser {

	private final HttpServletRequest req;
	private final Hashtable<String, String> cookieJar = new Hashtable<String, String>();

	/**
	 * Constructs a new CookieParser to handle the cookies of the given request.
	 * 
	 * @param req
	 *        the servlet request
	 */
	public CookieParser(final HttpServletRequest req) {
		this.req = req;
		parseCookies();
	}

	// Load the cookie values into the cookies hashtable
	void parseCookies() {
		final Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				final String name = cookies[i].getName();
				final String value = cookies[i].getValue();
				cookieJar.put(name, value);
			}
		}
	}

	/**
	 * Gets the named cookie value as a String
	 * 
	 * @param name
	 *        the cookie name
	 * @return the cookie value as a String
	 * @exception CookieNotFoundException
	 *            if the cookie was not found
	 */
	public String getStringCookie(final String name) throws CookieNotFoundException {
		final String value = cookieJar.get(name);
		if (value == null) {
			throw new CookieNotFoundException(name + " not found");
		} else {
			return value;
		}
	}

	/**
	 * Gets the named cookie value as a String, with a default. Returns the
	 * default value if the cookie is not found
	 * 
	 * @param name
	 *        the cookie name
	 * @param def
	 *        the default cookie value
	 * @return the cookie value as a String, or the default
	 */
	public String getStringCookie(final String name, final String def) {
		try {
			return getStringCookie(name);
		} catch (final Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named cookie value as a boolean
	 * 
	 * @param name
	 *        the cookie name
	 * @return the cookie value as a boolean
	 * @exception CookieNotFoundException
	 *            if the cookie was not found
	 */
	public boolean getBooleanCookie(final String name) throws CookieNotFoundException {
		return new Boolean(getStringCookie(name)).booleanValue();
	}

	/**
	 * Gets the named cookie value as a boolean, with a default. Returns the
	 * default value if the cookie is not found.
	 * 
	 * @param name
	 *        the cookie name
	 * @param def
	 *        the default cookie value
	 * @return the cookie value as a boolean, or the default
	 */
	public boolean getBooleanCookie(final String name, final boolean def) {
		try {
			return getBooleanCookie(name);
		} catch (final Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named cookie value as a byte
	 * 
	 * @param name
	 *        the cookie name
	 * @return the cookie value as a byte
	 * @exception CookieNotFoundException
	 *            if the cookie was not found
	 * @exception NumberFormatException
	 *            if the cookie value could not be converted to a byte
	 */
	public byte getByteCookie(final String name) throws CookieNotFoundException,
			NumberFormatException {
		return Byte.parseByte(getStringCookie(name));
	}

	/**
	 * Gets the named cookie value as a byte, with a default. Returns the default
	 * value if the cookie is not found or cannot be converted to a byte.
	 * 
	 * @param name
	 *        the cookie name
	 * @param def
	 *        the default cookie value
	 * @return the cookie value as a byte, or the default
	 */
	public byte getByteCookie(final String name, final byte def) {
		try {
			return getByteCookie(name);
		} catch (final Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named cookie value as a char
	 * 
	 * @param name
	 *        the cookie name
	 * @return the cookie value as a char
	 * @exception CookieNotFoundException
	 *            if the cookie was not found
	 */
	public char getCharCookie(final String name) throws CookieNotFoundException {
		final String param = getStringCookie(name);
		if (param.length() == 0) {
			throw new CookieNotFoundException(name + " is empty string");
		} else {
			return (param.charAt(0));
		}
	}

	/**
	 * Gets the named cookie value as a char, with a default. Returns the default
	 * value if the cookie is not found.
	 * 
	 * @param name
	 *        the cookie name
	 * @param def
	 *        the default cookie value
	 * @return the cookie value as a char, or the default
	 */
	public char getCharCookie(final String name, final char def) {
		try {
			return getCharCookie(name);
		} catch (final Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named cookie value as a double
	 * 
	 * @param name
	 *        the cookie name
	 * @return the cookie value as a double
	 * @exception CookieNotFoundException
	 *            if the cookie was not found
	 * @exception NumberFormatException
	 *            if the cookie could not be converted to a double
	 */
	public double getDoubleCookie(final String name) throws CookieNotFoundException,
			NumberFormatException {
		return new Double(getStringCookie(name)).doubleValue();
	}

	/**
	 * Gets the named cookie value as a double, with a default. Returns the
	 * default value if the cookie is not found.
	 * 
	 * @param name
	 *        the cookie name
	 * @param def
	 *        the default cookie value
	 * @return the cookie value as a double, or the default
	 */
	public double getDoubleCookie(final String name, final double def) {
		try {
			return getDoubleCookie(name);
		} catch (final Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named cookie value as a float
	 * 
	 * @param name
	 *        the cookie name
	 * @return the cookie value as a float
	 * @exception CookieNotFoundException
	 *            if the cookie was not found
	 * @exception NumberFormatException
	 *            if the cookie could not be converted to a float
	 */
	public float getFloatCookie(final String name) throws CookieNotFoundException,
			NumberFormatException {
		return new Float(getStringCookie(name)).floatValue();
	}

	/**
	 * Gets the named cookie value as a float, with a default. Returns the
	 * default value if the cookie is not found.
	 * 
	 * @param name
	 *        the cookie name
	 * @param def
	 *        the default cookie value
	 * @return the cookie value as a float, or the default
	 */
	public float getFloatCookie(final String name, final float def) {
		try {
			return getFloatCookie(name);
		} catch (final Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named cookie value as a int
	 * 
	 * @param name
	 *        the cookie name
	 * @return the cookie value as a int
	 * @exception CookieNotFoundException
	 *            if the cookie was not found
	 * @exception NumberFormatException
	 *            if the cookie could not be converted to a int
	 */
	public int getIntCookie(final String name) throws CookieNotFoundException, NumberFormatException {
		return Integer.parseInt(getStringCookie(name));
	}

	/**
	 * Gets the named cookie value as a int, with a default. Returns the default
	 * value if the cookie is not found.
	 * 
	 * @param name
	 *        the cookie name
	 * @param def
	 *        the default cookie value
	 * @return the cookie value as a int, or the default
	 */
	public int getIntCookie(final String name, final int def) {
		try {
			return getIntCookie(name);
		} catch (final Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named cookie value as a long
	 * 
	 * @param name
	 *        the cookie name
	 * @return the cookie value as a long
	 * @exception CookieNotFoundException
	 *            if the cookie was not found
	 * @exception NumberFormatException
	 *            if the cookie could not be converted to a long
	 */
	public long getLongCookie(final String name) throws CookieNotFoundException,
			NumberFormatException {
		return Long.parseLong(getStringCookie(name));
	}

	/**
	 * Gets the named cookie value as a long, with a default. Returns the default
	 * value if the cookie is not found.
	 * 
	 * @param name
	 *        the cookie name
	 * @param def
	 *        the default cookie value
	 * @return the cookie value as a long, or the default
	 */
	public long getLongCookie(final String name, final long def) {
		try {
			return getLongCookie(name);
		} catch (final Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named cookie value as a short
	 * 
	 * @param name
	 *        the cookie name
	 * @return the cookie value as a short
	 * @exception CookieNotFoundException
	 *            if the cookie was not found
	 * @exception NumberFormatException
	 *            if the cookie could not be converted to a short
	 */
	public short getShortCookie(final String name) throws CookieNotFoundException,
			NumberFormatException {
		return Short.parseShort(getStringCookie(name));
	}

	/**
	 * Gets the named cookie value as a short, with a default. Returns the
	 * default value if the cookie is not found.
	 * 
	 * @param name
	 *        the cookie name
	 * @param def
	 *        the default cookie value
	 * @return the cookie value as a short, or the default
	 */
	public short getShortCookie(final String name, final short def) {
		try {
			return getShortCookie(name);
		} catch (final Exception e) {
			return def;
		}
	}
}

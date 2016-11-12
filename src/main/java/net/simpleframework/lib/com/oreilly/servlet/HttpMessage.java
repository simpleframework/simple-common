// Copyright (C) 1998-2001 by Jason Hunter <jhunter_AT_acm_DOT_org>.
// All rights reserved. Use of this class is limited.
// Please see the LICENSE for more information.

package net.simpleframework.lib.com.oreilly.servlet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import net.simpleframework.common.Base64;

/**
 * A class to simplify HTTP applet-server communication. It abstracts
 * the communication into messages, which can be either GET or POST.
 * <p>
 * It can be used like this:
 * <blockquote>
 * 
 * <pre>
 * URL url = new URL(getCodeBase(), "/servlet/ServletName");
 * 
 * HttpMessage msg = new HttpMessage(url);
 * 
 * // Parameters may optionally be set using java.util.Properties
 * Properties props = new Properties();
 * props.put("name", "value");
 * 
 * // Headers, cookies, and authorization may be set as well
 * msg.setHeader("Accept", "image/png"); // optional
 * msg.setCookie("JSESSIONID", "9585155923883872"); // optional
 * msg.setAuthorization("guest", "try2gueSS"); // optional
 * 
 * InputStream in = msg.sendGetMessage(props);
 * </pre>
 * 
 * </blockquote>
 * <p>
 * This class is loosely modeled after the ServletMessage class written
 * by Rod McChesney of JavaSoft.
 *
 * @author <b>Jason Hunter</b>, Copyright &#169; 1998
 * @version 1.3, 2000/10/24, fixed headers NPE bug
 * @version 1.2, 2000/10/15, changed uploaded object MIME type to
 *          application/x-java-serialized-object
 * @version 1.1, 2000/06/11, added ability to set headers, cookies,
 *          and authorization
 * @version 1.0, 1998/09/18
 */
public class HttpMessage {

	URL servlet = null;
	Hashtable headers = null;

	/**
	 * Constructs a new HttpMessage that can be used to communicate with the
	 * servlet at the specified URL.
	 *
	 * @param servlet
	 *        the server resource (typically a servlet) with which
	 *        to communicate
	 */
	public HttpMessage(final URL servlet) {
		this.servlet = servlet;
	}

	/**
	 * Performs a GET request to the servlet, with no query string.
	 *
	 * @return an InputStream to read the response
	 * @exception IOException
	 *            if an I/O error occurs
	 */
	public InputStream sendGetMessage() throws IOException {
		return sendGetMessage(null);
	}

	/**
	 * Performs a GET request to the servlet, building
	 * a query string from the supplied properties list.
	 *
	 * @param args
	 *        the properties list from which to build a query string
	 * @return an InputStream to read the response
	 * @exception IOException
	 *            if an I/O error occurs
	 */
	public InputStream sendGetMessage(final Properties args) throws IOException {
		String argString = ""; // default

		if (args != null) {
			argString = "?" + toEncodedString(args);
		}
		final URL url = new URL(servlet.toExternalForm() + argString);

		// Turn off caching
		final URLConnection con = url.openConnection();
		con.setUseCaches(false);

		// Send headers
		sendHeaders(con);

		return con.getInputStream();
	}

	/**
	 * Performs a POST request to the servlet, with no query string.
	 *
	 * @return an InputStream to read the response
	 * @exception IOException
	 *            if an I/O error occurs
	 */
	public InputStream sendPostMessage() throws IOException {
		return sendPostMessage(null);
	}

	/**
	 * Performs a POST request to the servlet, building
	 * post data from the supplied properties list.
	 *
	 * @param args
	 *        the properties list from which to build the post data
	 * @return an InputStream to read the response
	 * @exception IOException
	 *            if an I/O error occurs
	 */
	public InputStream sendPostMessage(final Properties args) throws IOException {
		String argString = ""; // default
		if (args != null) {
			argString = toEncodedString(args); // notice no "?"
		}

		final URLConnection con = servlet.openConnection();

		// Prepare for both input and output
		con.setDoInput(true);
		con.setDoOutput(true);

		// Turn off caching
		con.setUseCaches(false);

		// Work around a Netscape bug
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		// Send headers
		sendHeaders(con);

		// Write the arguments as post data
		final DataOutputStream out = new DataOutputStream(con.getOutputStream());
		out.writeBytes(argString);
		out.flush();
		out.close();

		return con.getInputStream();
	}

	/**
	 * Performs a POST request to the servlet, uploading a serialized object.
	 * <p>
	 * The servlet can receive the object in its <tt>doPost()</tt> method
	 * like this:
	 * 
	 * <pre>
	 * ObjectInputStream objin = new ObjectInputStream(req.getInputStream());
	 * Object obj = objin.readObject();
	 * </pre>
	 * 
	 * The type of the uploaded object can be determined through introspection.
	 *
	 * @param obj
	 *        the serializable object to upload
	 * @return an InputStream to read the response
	 * @exception IOException
	 *            if an I/O error occurs
	 */
	public InputStream sendPostMessage(final Serializable obj) throws IOException {
		final URLConnection con = servlet.openConnection();

		// Prepare for both input and output
		con.setDoInput(true);
		con.setDoOutput(true);

		// Turn off caching
		con.setUseCaches(false);

		// Set the content type to be application/x-java-serialized-object
		con.setRequestProperty("Content-Type", "application/x-java-serialized-object");

		// Send headers
		sendHeaders(con);

		// Write the serialized object as post data
		final ObjectOutputStream out = new ObjectOutputStream(con.getOutputStream());
		out.writeObject(obj);
		out.flush();
		out.close();

		return con.getInputStream();
	}

	/**
	 * Sets a request header with the given name and value. The header
	 * persists across multiple requests. The caller is responsible for
	 * ensuring there are no illegal characters in the name and value.
	 *
	 * @param name
	 *        the header name
	 * @param value
	 *        the header value
	 */
	public void setHeader(final String name, final String value) {
		if (headers == null) {
			headers = new Hashtable();
		}
		headers.put(name, value);
	}

	// Send the contents of the headers hashtable to the server
	private void sendHeaders(final URLConnection con) {
		if (headers != null) {
			final Enumeration enumm = headers.keys();
			while (enumm.hasMoreElements()) {
				final String name = (String) enumm.nextElement();
				final String value = (String) headers.get(name);
				con.setRequestProperty(name, value);
			}
		}
	}

	/**
	 * Sets a request cookie with the given name and value. The cookie
	 * persists across multiple requests. The caller is responsible for
	 * ensuring there are no illegal characters in the name and value.
	 *
	 * @param name
	 *        the header name
	 * @param value
	 *        the header value
	 */
	public void setCookie(final String name, final String value) {
		if (headers == null) {
			headers = new Hashtable();
		}
		final String existingCookies = (String) headers.get("Cookie");
		if (existingCookies == null) {
			setHeader("Cookie", name + "=" + value);
		} else {
			setHeader("Cookie", existingCookies + "; " + name + "=" + value);
		}
	}

	/**
	 * Sets the authorization information for the request (using BASIC
	 * authentication via the HTTP Authorization header). The authorization
	 * persists across multiple requests.
	 *
	 * @param name
	 *        the user name
	 * @param password
	 *        the user password
	 */
	public void setAuthorization(final String name, final String password) {
		final String authorization = Base64.encodeToString(name + ":" + password);
		setHeader("Authorization", "Basic " + authorization);
	}

	/*
	 * Converts a properties list to a URL-encoded query string
	 */
	private String toEncodedString(final Properties args) {
		final StringBuffer buf = new StringBuffer();
		final Enumeration names = args.propertyNames();
		while (names.hasMoreElements()) {
			final String name = (String) names.nextElement();
			final String value = args.getProperty(name);
			buf.append(URLEncoder.encode(name) + "=" + URLEncoder.encode(value));
			if (names.hasMoreElements()) {
				buf.append("&");
			}
		}
		return buf.toString();
	}
}

// Copyright (C) 1999-2001 by Jason Hunter <jhunter_AT_acm_DOT_org>.
// All rights reserved. Use of this class is limited.
// Please see the LICENSE for more information.

package net.simpleframework.lib.com.oreilly.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A superclass for HTTP servlets that wish to have their output
 * cached and automatically resent as appropriate according to the
 * servlet's getLastModified() method. To take advantage of this class,
 * a servlet must:
 * <ul>
 * <li>Extend <tt>CacheHttpServlet</tt> instead of <tt>HttpServlet</tt>
 * <li>Implement a <tt>getLastModified(HttpServletRequest)</tt> method as usual
 * </ul>
 * This class uses the value returned by <tt>getLastModified()</tt> to manage
 * an internal cache of the servlet's output. Before handling a request,
 * this class checks the value of <tt>getLastModified()</tt>, and if the
 * output cache is at least as current as the servlet's last modified time,
 * the cached output is sent without calling the servlet's <tt>doGet()</tt>
 * method.
 * <p>
 * In order to be safe, if this class detects that the servlet's query
 * string, extra path info, or servlet path has changed, the cache is
 * invalidated and recreated. However, this class does not invalidate
 * the cache based on differing request headers or cookies; for
 * servlets that vary their output based on these values (i.e. a session
 * tracking servlet) this class should probably not be used.
 * <p>
 * No caching is performed for POST requests.
 * <p>
 * <tt>CacheHttpServletResponse</tt> and <tt>CacheServletOutputStream</tt>
 * are helper classes to this class and should not be used directly.
 * <p>
 * This class has been built against Servlet API 2.2. Using it with previous
 * Servlet API versions should work; using it with future API versions likely
 * won't work.
 *
 * @author <b>Jason Hunter</b>, Copyright &#169; 1999
 * @version 0.93, 2004/06/25, added setCharacterEncoding() for servlets 2.4
 * @version 0.92, 2000/03/16, added synchronization blocks to make thread safe
 * @version 0.91, 1999/12/28, made support classes package protected
 * @version 0.90, 1999/12/19
 */

public abstract class CacheHttpServlet extends HttpServlet {

	CacheHttpServletResponse cacheResponse;
	long cacheLastMod = -1;
	String cacheQueryString = null;
	String cachePathInfo = null;
	String cacheServletPath = null;
	Object lock = new Object();

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse res)
			throws ServletException, IOException {
		// Only do caching for GET requests
		final String method = req.getMethod();
		if (!method.equals("GET")) {
			super.service(req, res);
			return;
		}

		// Check the last modified time for this servlet
		final long servletLastMod = getLastModified(req);

		// A last modified of -1 means we shouldn't use any cache logic
		if (servletLastMod == -1) {
			super.service(req, res);
			return;
		}

		// If the client sent an If-Modified-Since header equal or after the
		// servlet's last modified time, send a short "Not Modified" status code
		// Round down to the nearest second since client headers are in seconds
		if ((servletLastMod / 1000 * 1000) <= req.getDateHeader("If-Modified-Since")) {
			res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}

		// Use the existing cache if it's current and valid
		CacheHttpServletResponse localResponseCopy = null;
		synchronized (lock) {
			if (servletLastMod <= cacheLastMod && cacheResponse.isValid()
					&& equal(cacheQueryString, req.getQueryString())
					&& equal(cachePathInfo, req.getPathInfo())
					&& equal(cacheServletPath, req.getServletPath())) {
				localResponseCopy = cacheResponse;
			}
		}
		if (localResponseCopy != null) {
			localResponseCopy.writeTo(res);
			return;
		}

		// Otherwise make a new cache to capture the response
		localResponseCopy = new CacheHttpServletResponse(res);
		super.service(req, localResponseCopy);
		synchronized (lock) {
			cacheResponse = localResponseCopy;
			cacheLastMod = servletLastMod;
			cacheQueryString = req.getQueryString();
			cachePathInfo = req.getPathInfo();
			cacheServletPath = req.getServletPath();
		}
	}

	private boolean equal(final String s1, final String s2) {
		if (s1 == null && s2 == null) {
			return true;
		} else if (s1 == null || s2 == null) {
			return false;
		} else {
			return s1.equals(s2);
		}
	}
}

class CacheHttpServletResponse implements HttpServletResponse {
	// Store key response variables so they can be set later
	private int status;
	private Hashtable headers;
	private int contentLength;
	private String contentType;
	private String encoding;
	private Locale locale;
	private Vector cookies;
	private boolean didError;
	private boolean didRedirect;
	private boolean gotStream;
	private boolean gotWriter;

	private final HttpServletResponse delegate;
	private CacheServletOutputStream out;
	private PrintWriter writer;

	CacheHttpServletResponse(final HttpServletResponse res) {
		delegate = res;
		try {
			out = new CacheServletOutputStream(res.getOutputStream());
		} catch (final IOException e) {
			System.out.println("Got IOException constructing cached response: " + e.getMessage());
		}
		internalReset();
	}

	private void internalReset() {
		status = 200;
		headers = new Hashtable();
		contentLength = -1;
		contentType = null;
		encoding = null;
		locale = null;
		cookies = new Vector();
		didError = false;
		didRedirect = false;
		gotStream = false;
		gotWriter = false;
		out.getBuffer().reset();
	}

	public boolean isValid() {
		// We don't cache error pages or redirects
		return didError != true && didRedirect != true;
	}

	private void internalSetHeader(final String name, final Object value) {
		final Vector v = new Vector();
		v.addElement(value);
		headers.put(name, v);
	}

	private void internalAddHeader(final String name, final Object value) {
		Vector v = (Vector) headers.get(name);
		if (v == null) {
			v = new Vector();
		}
		v.addElement(value);
		headers.put(name, v);
	}

	public void writeTo(final HttpServletResponse res) {
		// Write status code
		res.setStatus(status);
		// Write convenience headers
		if (contentType != null) {
			res.setContentType(contentType);
		}
		if (encoding != null) {
			res.setCharacterEncoding(encoding);
		}
		if (locale != null) {
			res.setLocale(locale);
		}
		// Write cookies
		Enumeration enu = cookies.elements();
		while (enu.hasMoreElements()) {
			final Cookie c = (Cookie) enu.nextElement();
			res.addCookie(c);
		}
		// Write standard headers
		enu = headers.keys();
		while (enu.hasMoreElements()) {
			final String name = (String) enu.nextElement();
			final Vector values = (Vector) headers.get(name); // may have multiple
																				// values
			final Enumeration enu2 = values.elements();
			while (enu2.hasMoreElements()) {
				final Object value = enu2.nextElement();
				if (value instanceof String) {
					res.setHeader(name, (String) value);
				}
				if (value instanceof Integer) {
					res.setIntHeader(name, ((Integer) value).intValue());
				}
				if (value instanceof Long) {
					res.setDateHeader(name, ((Long) value).longValue());
				}
			}
		}
		// Write content length
		res.setContentLength(out.getBuffer().size());
		// Write body
		try {
			out.getBuffer().writeTo(res.getOutputStream());
		} catch (final IOException e) {
			System.out.println("Got IOException writing cached response: " + e.getMessage());
		}
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (gotWriter) {
			throw new IllegalStateException("Cannot get output stream after getting writer");
		}
		gotStream = true;
		return out;
	}

	@Override
	public PrintWriter getWriter() throws UnsupportedEncodingException {
		if (gotStream) {
			throw new IllegalStateException("Cannot get writer after getting output stream");
		}
		gotWriter = true;
		if (writer == null) {
			final OutputStreamWriter w = new OutputStreamWriter(out, getCharacterEncoding());
			writer = new PrintWriter(w, true); // autoflush is necessary
		}
		return writer;
	}

	@Override
	public void setContentLength(final int len) {
		delegate.setContentLength(len);
		// No need to save the length; we can calculate it later
	}

	@Override
	public void setContentType(final String type) {
		delegate.setContentType(type);
		contentType = type;
	}

	@Override
	public void setCharacterEncoding(final String encoding) {
		delegate.setCharacterEncoding(encoding);
		this.encoding = encoding;
	}

	@Override
	public String getCharacterEncoding() {
		return delegate.getCharacterEncoding();
	}

	@Override
	public void setBufferSize(final int size) throws IllegalStateException {
		delegate.setBufferSize(size);
	}

	@Override
	public int getBufferSize() {
		return delegate.getBufferSize();
	}

	@Override
	public void reset() throws IllegalStateException {
		delegate.reset();
		internalReset();
	}

	@Override
	public void resetBuffer() throws IllegalStateException {
		delegate.resetBuffer();
		contentLength = -1;
		out.getBuffer().reset();
	}

	@Override
	public boolean isCommitted() {
		return delegate.isCommitted();
	}

	@Override
	public void flushBuffer() throws IOException {
		delegate.flushBuffer();
	}

	@Override
	public void setLocale(final Locale loc) {
		delegate.setLocale(loc);
		locale = loc;
	}

	@Override
	public Locale getLocale() {
		return delegate.getLocale();
	}

	@Override
	public void addCookie(final Cookie cookie) {
		delegate.addCookie(cookie);
		cookies.addElement(cookie);
	}

	@Override
	public boolean containsHeader(final String name) {
		return delegate.containsHeader(name);
	}

	@Override
	public String getContentType() {
		return delegate.getContentType();
	}

	/** @deprecated */
	@Deprecated
	@Override
	public void setStatus(final int sc, final String sm) {
		delegate.setStatus(sc, sm);
		status = sc;
	}

	@Override
	public void setStatus(final int sc) {
		delegate.setStatus(sc);
		status = sc;
	}

	@Override
	public void setHeader(final String name, final String value) {
		delegate.setHeader(name, value);
		internalSetHeader(name, value);
	}

	@Override
	public void setIntHeader(final String name, final int value) {
		delegate.setIntHeader(name, value);
		internalSetHeader(name, new Integer(value));
	}

	@Override
	public void setDateHeader(final String name, final long date) {
		delegate.setDateHeader(name, date);
		internalSetHeader(name, new Long(date));
	}

	@Override
	public void sendError(final int sc, final String msg) throws IOException {
		delegate.sendError(sc, msg);
		didError = true;
	}

	@Override
	public void sendError(final int sc) throws IOException {
		delegate.sendError(sc);
		didError = true;
	}

	@Override
	public void sendRedirect(final String location) throws IOException {
		delegate.sendRedirect(location);
		didRedirect = true;
	}

	@Override
	public String encodeURL(final String url) {
		return delegate.encodeURL(url);
	}

	@Override
	public String encodeRedirectURL(final String url) {
		return delegate.encodeRedirectURL(url);
	}

	@Override
	public void addHeader(final String name, final String value) {
		internalAddHeader(name, value);
	}

	@Override
	public void addIntHeader(final String name, final int value) {
		internalAddHeader(name, new Integer(value));
	}

	@Override
	public void addDateHeader(final String name, final long value) {
		internalAddHeader(name, new Long(value));
	}

	/** @deprecated */
	@Deprecated
	@Override
	public String encodeUrl(final String url) {
		return this.encodeURL(url);
	}

	/** @deprecated */
	@Deprecated
	@Override
	public String encodeRedirectUrl(final String url) {
		return this.encodeRedirectURL(url);
	}
}

class CacheServletOutputStream extends ServletOutputStream {

	ServletOutputStream delegate;
	ByteArrayOutputStream cache;

	CacheServletOutputStream(final ServletOutputStream out) {
		delegate = out;
		cache = new ByteArrayOutputStream(4096);
	}

	public ByteArrayOutputStream getBuffer() {
		return cache;
	}

	@Override
	public void write(final int b) throws IOException {
		delegate.write(b);
		cache.write(b);
	}

	@Override
	public void write(final byte b[]) throws IOException {
		delegate.write(b);
		cache.write(b);
	}

	@Override
	public void write(final byte buf[], final int offset, final int len) throws IOException {
		delegate.write(buf, offset, len);
		cache.write(buf, offset, len);
	}
}

// Copyright (C) 1998-2001 by Jason Hunter <jhunter_AT_acm_DOT_org>.
// All rights reserved. Use of this class is limited.
// Please see the LICENSE for more information.

package net.simpleframework.lib.com.oreilly.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * A request wrapper to support MultipartFilter.
 * The filter capability requires Servlet API 2.3.
 * <p>
 * See Jason Hunter's June 2001 article in JavaWorld for a full explanation of
 * the class usage.
 *
 * @author <b>Jason Hunter</b>, Copyright &#169; 2001
 * @version 1.1, 2002/11/15, added getOriginalFileName() to match
 *          MultipartRequest
 * @version 1.0, 2001/06/19
 */
public class MultipartWrapper extends HttpServletRequestWrapper {

	MultipartRequest mreq = null;

	public MultipartWrapper(final HttpServletRequest req, final String dir) throws IOException {
		super(req);
		mreq = new MultipartRequest(req, dir);
	}

	// Methods to replace HSR methods
	@Override
	public Enumeration getParameterNames() {
		return mreq.getParameterNames();
	}

	@Override
	public String getParameter(final String name) {
		return mreq.getParameter(name);
	}

	@Override
	public String[] getParameterValues(final String name) {
		return mreq.getParameterValues(name);
	}

	@Override
	public Map getParameterMap() {
		final Map map = new HashMap();
		final Enumeration enumm = getParameterNames();
		while (enumm.hasMoreElements()) {
			final String name = (String) enumm.nextElement();
			map.put(name, mreq.getParameterValues(name));
		}
		return map;
	}

	// Methods only in MultipartRequest
	public Enumeration getFileNames() {
		return mreq.getFileNames();
	}

	public String getFilesystemName(final String name) {
		return mreq.getFilesystemName(name);
	}

	public String getOriginalFileName(final String name) {
		return mreq.getOriginalFileName(name);
	}

	public String getContentType(final String name) {
		return mreq.getContentType(name);
	}

	public File getFile(final String name) {
		return mreq.getFile(name);
	}
}

// Copyright (C) 2001 by Jason Hunter <jhunter_AT_acm_DOT_org>.
// All rights reserved. Use of this class is limited.
// Please see the LICENSE for more information.

package net.simpleframework.lib.com.oreilly.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * A filter for easy semi-automatic handling of multipart/form-data requests
 * (file uploads). The filter capability requires Servlet API 2.3.
 * <p>
 * See Jason Hunter's June 2001 article in JavaWorld for a full explanation of
 * the class usage.
 * 
 * @author <b>Jason Hunter</b>, Copyright &#169; 2001
 * @version 1.0, 2001/06/19
 */
public class MultipartFilter implements Filter {

	FilterConfig config = null;
	private String dir = null;

	@Override
	public void init(final FilterConfig config) throws ServletException {
		this.config = config;

		// Determine the upload directory. First look for an uploadDir filter
		// init parameter. Then look for the context tempdir.
		dir = config.getInitParameter("uploadDir");
		if (dir == null) {
			final File tempdir = (File) config.getServletContext().getAttribute(
					"javax.servlet.context.tempdir");
			if (tempdir != null) {
				dir = tempdir.toString();
			} else {
				throw new ServletException(
						"MultipartFilter: No upload directory found: set an uploadDir "
								+ "init parameter or ensure the javax.servlet.context.tempdir "
								+ "directory is valid");
			}
		}
	}

	@Override
	public void destroy() {
		config = null;
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response,
			final FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest req = (HttpServletRequest) request;
		final String type = req.getHeader("Content-Type");

		// If this is not a multipart/form-data request continue
		if (type == null || !type.startsWith("multipart/form-data")) {
			chain.doFilter(request, response);
		} else {
			final MultipartWrapper multi = new MultipartWrapper(req, dir);
			chain.doFilter(multi, response);
		}
	}
}

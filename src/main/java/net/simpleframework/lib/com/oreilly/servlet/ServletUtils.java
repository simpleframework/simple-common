// Copyright (C) 1998-2001 by Jason Hunter <jhunter_AT_acm_DOT_org>.
// All rights reserved. Use of this class is limited.
// Please see the LICENSE for more information.

package net.simpleframework.lib.com.oreilly.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletContext;

/**
 * A collection of static utility methods useful to servlets. Some methods
 * require Servlet API 2.2.
 * 
 * @author <b>Jason Hunter</b>, Copyright &#169; 1998-2000
 * @version 1.5, 2001/02/11, added getResource() ".." check
 * @version 1.4, 2000/09/27, finalized getResource() behavior
 * @version 1.3, 2000/08/15, improved getStackTraceAsString() to take Throwable
 * @version 1.2, 2000/03/10, added getResource() method
 * @version 1.1, 2000/02/13, added returnURL() methods
 * @version 1.0, 1098/09/18
 */
public class ServletUtils {

	/**
	 * Sends the contents of the specified file to the output stream
	 * 
	 * @param filename
	 *        the file to send
	 * @param out
	 *        the output stream to write the file
	 * @exception FileNotFoundException
	 *            if the file does not exist
	 * @exception IOException
	 *            if an I/O error occurs
	 */
	public static void returnFile(final String filename, final OutputStream out)
			throws FileNotFoundException, IOException {
		// A FileInputStream is for bytes
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filename);
			final byte[] buf = new byte[4 * 1024]; // 4K buffer
			int bytesRead;
			while ((bytesRead = fis.read(buf)) != -1) {
				out.write(buf, 0, bytesRead);
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	/**
	 * Sends the contents of the specified URL to the output stream
	 * 
	 * @param URL
	 *        whose contents are to be sent
	 * @param out
	 *        the output stream to write the contents
	 * @exception IOException
	 *            if an I/O error occurs
	 */
	public static void returnURL(final URL url, final OutputStream out) throws IOException {
		final InputStream in = url.openStream();
		final byte[] buf = new byte[4 * 1024]; // 4K buffer
		int bytesRead;
		while ((bytesRead = in.read(buf)) != -1) {
			out.write(buf, 0, bytesRead);
		}
	}

	/**
	 * Sends the contents of the specified URL to the Writer (commonly either a
	 * PrintWriter or JspWriter)
	 * 
	 * @param URL
	 *        whose contents are to be sent
	 * @param out
	 *        the Writer to write the contents
	 * @exception IOException
	 *            if an I/O error occurs
	 */
	public static void returnURL(final URL url, final Writer out) throws IOException {
		// Determine the URL's content encoding
		final URLConnection con = url.openConnection();
		con.connect();
		final String encoding = con.getContentEncoding();

		// Construct a Reader appropriate for that encoding
		BufferedReader in = null;
		if (encoding == null) {
			in = new BufferedReader(new InputStreamReader(url.openStream()));
		} else {
			in = new BufferedReader(new InputStreamReader(url.openStream(), encoding));
		}
		final char[] buf = new char[4 * 1024]; // 4Kchar buffer
		int charsRead;
		while ((charsRead = in.read(buf)) != -1) {
			out.write(buf, 0, charsRead);
		}
	}

	/**
	 * Gets an exception's stack trace as a String
	 * 
	 * @param e
	 *        the exception
	 * @return the stack trace of the exception
	 */
	public static String getStackTraceAsString(final Throwable t) {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		final PrintWriter writer = new PrintWriter(bytes, true);
		t.printStackTrace(writer);
		return bytes.toString();
	}

	/**
	 * Gets a reference to the given resource within the given context, making
	 * sure not to serve the contents of WEB-INF, META-INF, or to display .jsp
	 * file source. Throws an IOException if the resource can't be read.
	 * 
	 * @param context
	 *        the context containing the resource
	 * @param resource
	 *        the resource to be read
	 * @return a URL reference to the resource
	 * @exception IOException
	 *            if there's any problem accessing the resource
	 */
	public static URL getResource(final ServletContext context, final String resource)
			throws IOException {
		// Short-circuit if resource is null
		if (resource == null) {
			throw new FileNotFoundException("Requested resource was null (passed in null)");
		}

		if (resource.endsWith("/") || resource.endsWith("\\") || resource.endsWith(".")) {
			throw new MalformedURLException("Path may not end with a slash or dot");
		}

		if (resource.indexOf("..") != -1) {
			throw new MalformedURLException("Path may not contain double dots");
		}

		final String upperResource = resource.toUpperCase();
		if (upperResource.startsWith("/WEB-INF") || upperResource.startsWith("/META-INF")) {
			throw new MalformedURLException("Path may not begin with /WEB-INF or /META-INF");
		}

		if (upperResource.endsWith(".JSP")) {
			throw new MalformedURLException("Path may not end with .jsp");
		}

		// Convert the resource to a URL
		final URL url = context.getResource(resource);
		if (url == null) {
			throw new FileNotFoundException("Requested resource was null (" + resource + ")");
		}

		return url;
	}
}

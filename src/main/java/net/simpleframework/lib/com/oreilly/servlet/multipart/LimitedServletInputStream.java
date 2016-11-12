// Copyright (C) 1999-2001 by Jason Hunter <jhunter_AT_acm_DOT_org>.
// All rights reserved. Use of this class is limited.
// Please see the LICENSE for more information.

package net.simpleframework.lib.com.oreilly.servlet.multipart;

import java.io.IOException;

import javax.servlet.ServletInputStream;

/**
 * A <code>LimitedServletInputStream</code> wraps another
 * <code>ServletInputStream</code> in order to keep track of how many bytes
 * have been read and detect when the Content-Length limit has been reached.
 * This is necessary since some servlet containers are slow to notice the end
 * of stream and cause the client code to hang if it tries to read past it.
 * 
 * @author Jason Hunter
 * @author Geoff Soutter
 * @version 1.0, 2000/10/27, initial revision
 */
public class LimitedServletInputStream extends ServletInputStream {

	/** input stream we are filtering */
	private final ServletInputStream in;

	/** number of bytes to read before giving up */
	private final int totalExpected;

	/** number of bytes we have currently read */
	private int totalRead = 0;

	/**
	 * Creates a <code>LimitedServletInputStream</code> with the specified
	 * length limit that wraps the provided <code>ServletInputStream</code>.
	 */
	public LimitedServletInputStream(final ServletInputStream in, final int totalExpected) {
		this.in = in;
		this.totalExpected = totalExpected;
	}

	/**
	 * Implement length limitation on top of the <code>readLine</code> method of
	 * the wrapped <code>ServletInputStream</code>.
	 *
	 * @param b
	 *        an array of bytes into which data is read.
	 * @param off
	 *        an integer specifying the character at which
	 *        this method begins reading.
	 * @param len
	 *        an integer specifying the maximum number of
	 *        bytes to read.
	 * @return an integer specifying the actual number of bytes
	 *         read, or -1 if the end of the stream is reached.
	 * @exception IOException
	 *            if an I/O error occurs.
	 */
	@Override
	public int readLine(final byte b[], final int off, final int len) throws IOException {
		int result;
		final int left = totalExpected - totalRead;
		if (left <= 0) {
			return -1;
		} else {
			result = in.readLine(b, off, Math.min(left, len));
		}
		if (result > 0) {
			totalRead += result;
		}
		return result;
	}

	/**
	 * Implement length limitation on top of the <code>read</code> method of
	 * the wrapped <code>ServletInputStream</code>.
	 *
	 * @return the next byte of data, or <code>-1</code> if the end of the
	 *         stream is reached.
	 * @exception IOException
	 *            if an I/O error occurs.
	 */
	@Override
	public int read() throws IOException {
		if (totalRead >= totalExpected) {
			return -1;
		}

		final int result = in.read();
		if (result != -1) {
			totalRead++;
		}
		return result;
	}

	/**
	 * Implement length limitation on top of the <code>read</code> method of
	 * the wrapped <code>ServletInputStream</code>.
	 *
	 * @param b
	 *        destination buffer.
	 * @param off
	 *        offset at which to start storing bytes.
	 * @param len
	 *        maximum number of bytes to read.
	 * @return the number of bytes read, or <code>-1</code> if the end of
	 *         the stream has been reached.
	 * @exception IOException
	 *            if an I/O error occurs.
	 */
	@Override
	public int read(final byte b[], final int off, final int len) throws IOException {
		int result;
		final int left = totalExpected - totalRead;
		if (left <= 0) {
			return -1;
		} else {
			result = in.read(b, off, Math.min(left, len));
		}
		if (result > 0) {
			totalRead += result;
		}
		return result;
	}
}

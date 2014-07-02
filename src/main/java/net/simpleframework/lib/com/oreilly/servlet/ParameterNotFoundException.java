// Copyright (C) 1998-2001 by Jason Hunter <jhunter_AT_acm_DOT_org>.
// All rights reserved. Use of this class is limited.
// Please see the LICENSE for more information.

package net.simpleframework.lib.com.oreilly.servlet;

/**
 * Thrown to indicate a parameter does not exist.
 * 
 * @see com.oreilly.servlet.ParameterParser
 * 
 * @author <b>Jason Hunter</b>, Copyright &#169; 1998
 * @version 1.0, 98/09/18
 */
public class ParameterNotFoundException extends Exception {

	/**
	 * Constructs a new ParameterNotFoundException with no detail message.
	 */
	public ParameterNotFoundException() {
		super();
	}

	/**
	 * Constructs a new ParameterNotFoundException with the specified detail
	 * message.
	 * 
	 * @param s
	 *        the detail message
	 */
	public ParameterNotFoundException(final String s) {
		super(s);
	}

	private static final long serialVersionUID = -2513892795005778575L;
}

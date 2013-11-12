package net.simpleframework.common.mail;

import net.simpleframework.common.th.RuntimeExceptionEx;

public class MailException extends RuntimeExceptionEx {

	public MailException(final String message, final Throwable t) {
		super(message, t);
	}

	public MailException(final Throwable t) {
		super(null, t);
	}

	private static final long serialVersionUID = -2872316741882860782L;
}
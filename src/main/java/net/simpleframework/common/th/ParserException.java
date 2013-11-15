package net.simpleframework.common.th;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class ParserException extends RuntimeExceptionEx {

	public ParserException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public static RuntimeException of(final Throwable throwable) {
		return _of(ParserException.class, null, throwable);
	}

	public static RuntimeException of(final String msg) {
		return _of(ParserException.class, msg);
	}

	private static final long serialVersionUID = 5520927549351783305L;
}

package net.simpleframework.common.th;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class ClassException extends RuntimeExceptionEx {

	public ClassException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public static ClassException of(final Throwable throwable) {
		return _of(ClassException.class, null, throwable);
	}

	private static final long serialVersionUID = 6128245380078511011L;
}

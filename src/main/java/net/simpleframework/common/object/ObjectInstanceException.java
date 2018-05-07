package net.simpleframework.common.object;

import net.simpleframework.common.th.RuntimeExceptionEx;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class ObjectInstanceException extends RuntimeExceptionEx {
	public ObjectInstanceException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public static ObjectInstanceException of(final Throwable throwable) {
		return _of(ObjectInstanceException.class, null, throwable);
	}

	private static final long serialVersionUID = -4969137027259374957L;
}

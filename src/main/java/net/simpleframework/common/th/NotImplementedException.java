package net.simpleframework.common.th;

import static net.simpleframework.common.I18n.$m;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NotImplementedException extends RuntimeExceptionEx {

	public NotImplementedException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public static NotImplementedException of(final String message) {
		return _of(NotImplementedException.class, message);
	}

	public static NotImplementedException of(final Class<?> objectClass, final String method) {
		return _of(NotImplementedException.class,
				$m("NotImplementedException.0", objectClass.getName(), method));
	}

	private static final long serialVersionUID = 6384740594048001510L;
}

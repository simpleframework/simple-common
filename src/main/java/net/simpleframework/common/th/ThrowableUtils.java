package net.simpleframework.common.th;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class ThrowableUtils {

	public static Throwable getCause(final Class<? extends Throwable> clazz,
			final Throwable throwable) {
		Throwable cause = throwable;
		while ((cause = cause.getCause()) != null && clazz.isAssignableFrom(cause.getClass())) {
			break;
		}
		return cause;
	}

	public static Throwable convertThrowable(Throwable th) {
		if (th instanceof UndeclaredThrowableException) {
			final Throwable throwable = ((UndeclaredThrowableException) th).getUndeclaredThrowable();
			if (throwable != null) {
				th = throwable;
			}
		} else if (th instanceof InvocationTargetException) {
			th = ((InvocationTargetException) th).getTargetException();
		}
		return th;
	}
}

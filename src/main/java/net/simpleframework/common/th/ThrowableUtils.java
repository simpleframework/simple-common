package net.simpleframework.common.th;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;

import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;

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
		while ((cause = cause.getCause()) != null) {
			if (clazz.isAssignableFrom(cause.getClass())) {
				break;
			}
		}
		return cause;
	}

	public static String getThrowableMessage(final Throwable th,
			final Map<Class<? extends Throwable>, String> msgs, final boolean trimline) {
		String message = null;
		Throwable th0 = th;
		while (th0 != null) {
			if (msgs != null) {
				message = msgs.get(th0.getClass());
			}
			if (!StringUtils.hasText(message)) {
				message = th0.getMessage();
			}
			if (StringUtils.hasText(message)) {
				break;
			}
			th0 = th0.getCause();
		}
		if (!StringUtils.hasText(message)) {
			message = Convert.toString(th);
		}
		if (trimline) {
			int pos = message.indexOf("\r");
			if (pos < 0) {
				pos = message.indexOf("\n");
			}
			if (pos > 0) {
				message = message.substring(0, pos);
			}
			message = message.trim();
		}
		return message;
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

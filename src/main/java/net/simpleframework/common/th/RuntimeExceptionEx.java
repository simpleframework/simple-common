package net.simpleframework.common.th;

import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@SuppressWarnings("serial")
public abstract class RuntimeExceptionEx extends RuntimeException {
	private final KVMap attributes = new KVMap();

	private int code;

	public RuntimeExceptionEx(final String msg, final Throwable cause) {
		super(StringUtils.hasText(msg) ? msg : msg(cause), cause);
	}

	public int getCode() {
		return code;
	}

	public RuntimeExceptionEx setCode(final int code) {
		this.code = code;
		return this;
	}

	public RuntimeExceptionEx putVal(final String key, final Object val) {
		attributes.add(key, val);
		return this;
	}

	public Object getVal(final String key) {
		return attributes.get(key);
	}

	@SuppressWarnings("unchecked")
	public static <T extends RuntimeExceptionEx> T _of(final Class<T> exClazz, final String msg) {
		return (T) _of(exClazz, msg, null);
	}

	public static RuntimeException _of(final Class<? extends RuntimeExceptionEx> exClazz,
			final String msg, final Throwable throwable) {
		if (throwable == null) {
			try {
				return exClazz.getConstructor(String.class, Throwable.class).newInstance(msg, null);
			} catch (final Exception e) {
			}
		}
		if (throwable instanceof RuntimeException) {
			return (RuntimeException) throwable;
		} else {
			try {
				return exClazz.getConstructor(String.class, Throwable.class).newInstance(msg,
						ThrowableUtils.convertThrowable(throwable));
			} catch (final Throwable e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private static String msg(final Throwable cause) {
		Throwable th = cause;
		while (th != null) {
			final String msg = th.getLocalizedMessage();
			if (StringUtils.hasText(msg)) {
				return msg;
			}
			th = th.getCause();
		}
		return null;
	}
}

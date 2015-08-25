package net.simpleframework.common.th;

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
		super(msg, cause);
	}

	public int getCode() {
		return code;
	}

	public RuntimeExceptionEx setCode(final int code) {
		this.code = code;
		return this;
	}

	public Object getVal(final String key) {
		return attributes.get(key);
	}

	public RuntimeExceptionEx putVal(final String key, final Object val) {
		attributes.add(key, val);
		return this;
	}

	@Override
	public String getLocalizedMessage() {
		String msg = super.getLocalizedMessage();
		Throwable cause;
		if (msg == null && (cause = getCause()) != null) {
			msg = cause.toString();
		}
		return msg;
	}

	public static <T extends RuntimeExceptionEx> T _of(final Class<T> exClazz, final String msg) {
		return _of(exClazz, msg, null);
	}

	@SuppressWarnings("unchecked")
	public static <T extends RuntimeExceptionEx> T _of(final Class<T> exClazz, final String msg,
			final Throwable throwable) {
		if (throwable == null) {
			try {
				return exClazz.getConstructor(String.class, Throwable.class).newInstance(msg, null);
			} catch (final Exception e) {
			}
		}
		try {
			if (throwable.getClass().equals(exClazz)) {
				return (T) throwable;
			}
			return exClazz.getConstructor(String.class, Throwable.class).newInstance(msg,
					ThrowableUtils.convertThrowable(throwable));
		} catch (final Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
}

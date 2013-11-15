package net.simpleframework.common.object;

import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import net.simpleframework.common.logger.Log;
import net.simpleframework.common.logger.LogFactory;
import net.simpleframework.common.th.NotImplementedException;
import net.simpleframework.common.th.RuntimeExceptionEx;
import net.simpleframework.common.th.ThrowableUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class ObjectEx {

	public static <T> T singleton(final Class<T> beanClass) {
		return ObjectFactory.singleton(beanClass);
	}

	public static Object singleton(final String className) {
		return ObjectFactory.singleton(className);
	}

	/**
	 * 扩充属性
	 */
	private Map<String, Object> attributes;

	public void enableAttributes() {
		attributes = new ConcurrentHashMap<String, Object>();
	}

	public Object getAttr(final String key) {
		return attributes != null ? attributes.get(key) : null;
	}

	public ObjectEx setAttr(final String key, final Object value) {
		if (attributes == null) {
			throw NotImplementedException.of(getClass(), "enableAttributes");
		}
		if (value == null) {
			removeAttr(key);
		} else {
			attributes.put(key, value);
		}
		return this;
	}

	public Object removeAttr(final String key) {
		return attributes != null ? attributes.remove(key) : null;
	}

	public void clearAttribute() {
		if (attributes != null) {
			attributes.clear();
		}
	}

	public Enumeration<String> attrNames() {
		return attributes != null ? new Vector<String>(attributes.keySet()).elements() : null;
	}

	public static <T> T isolate(final IIsolation<T> callback) {
		try {
			if (callback != null) {
				return callback.isolate();
			}
		} catch (final Throwable th) {
			final Throwable th2 = ThrowableUtils.convertThrowable(th);
			if (th2 instanceof ClassNotFoundException || th2 instanceof NoClassDefFoundError) {
				LogFactory.getLogger(ObjectEx.class).warn(th2.toString());
				return null;
			}
			if (th2 instanceof RuntimeException) {
				throw (RuntimeException) th2;
			} else {
				throw IsolationException.of(th2);
			}
		}
		return null;
	}

	public abstract static class VoidIsolation implements IIsolation<Object> {

		protected abstract void run() throws Exception;

		@Override
		public Object isolate() throws Exception {
			run();
			return null;
		}
	}

	public static interface IIsolation<T> {

		/**
		 * 隔离中运行
		 * 
		 * @throws Exception
		 */
		T isolate() throws Exception;
	}

	public static class IsolationException extends RuntimeExceptionEx {

		public IsolationException(final String msg, final Throwable cause) {
			super(msg, cause);
		}

		public static RuntimeException of(final Throwable throwable) {
			return _of(IsolationException.class, null, throwable);
		}

		private static final long serialVersionUID = 6418478949274739685L;
	}

	protected final Log log = LogFactory.getLogger(getClass());
}

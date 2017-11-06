package net.simpleframework.common.object;

import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import net.simpleframework.common.logger.Log;
import net.simpleframework.common.logger.LogFactory;
import net.simpleframework.common.object.ObjectFactory.IObjectCreatorListener;
import net.simpleframework.common.th.RuntimeExceptionEx;
import net.simpleframework.common.th.ThrowableUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class ObjectEx {

	public static <T> T singleton(final Class<T> beanClass,
			final IObjectCreatorListener... listeners) {
		return ObjectFactory.singleton(beanClass, listeners);
	}

	public static Object singleton(final String className,
			final IObjectCreatorListener... listeners) {
		return ObjectFactory.singleton(className, listeners);
	}

	/**
	 * 扩充属性
	 */
	private Map<String, Object> _attributes;

	private Map<String, Object> _getAttributes() {
		if (_attributes == null) {
			_attributes = new ConcurrentHashMap<>();
		}
		return _attributes;
	}

	public Object getAttr(final String key) {
		return _getAttributes().get(key);
	}

	public ObjectEx setAttr(final String key, final Object value) {
		if (value == null) {
			removeAttr(key);
		} else {
			_getAttributes().put(key, value);
		}
		return this;
	}

	public Object removeAttr(final String key) {
		return _getAttributes().remove(key);
	}

	public void clearAttribute() {
		_getAttributes().clear();
	}

	public Enumeration<String> attrNames() {
		return new Vector<>(_getAttributes().keySet()).elements();
	}

	public int attrSize() {
		return _getAttributes().size();
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttrCache(final String key, final CacheV<T> i) {
		T val = (T) getAttr(key);
		if (val == null && i.hasVal(val = i.get())) {
			setAttr(key, val);
		}
		return val;
	}

	public static abstract class CacheV<T> {
		public boolean hasVal(final T val) {
			return val != null;
		}

		public abstract T get();
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

		public static IsolationException of(final Throwable throwable) {
			return _of(IsolationException.class, null, throwable);
		}

		private static final long serialVersionUID = 6418478949274739685L;
	}

	protected Class<?> getOriginalClass() {
		return ObjectFactory.original(getClass());
	}

	private transient Log log;

	protected Log getLog() {
		if (log == null) {
			log = LogFactory.getLogger(getClass());
		}
		return log;
	}

	public static void oprintln() {
		System.out.println();
	}

	public static void oprintln(final Object o) {
		System.out.print("[System.out] ==> ");
		System.out.println(o);
	}
}

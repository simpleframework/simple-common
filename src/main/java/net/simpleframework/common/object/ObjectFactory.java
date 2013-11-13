package net.simpleframework.common.object;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.simpleframework.common.ClassUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class ObjectFactory {

	private static ObjectFactory factory = new ObjectFactory();

	public static ObjectFactory get() {
		return factory;
	}

	public static <T> T singleton(final Class<T> oClass) {
		return get()._singleton(oClass);
	}

	public static Object singleton(final String className) {
		return get()._singleton(className);
	}

	public static <T> T create(final Class<T> oClass) {
		return get()._create(oClass);
	}

	public static Object create(final String className) {
		return get()._create(className);
	}

	public static <T> T newInstance(final Class<T> oClass) {
		return get()._newInstance(oClass);
	}

	public static Class<?> original(final Class<?> proxy) {
		return get()._original(proxy);
	}

	private final Map<Class<?>, Object> singletonCache;
	{
		singletonCache = new ConcurrentHashMap<Class<?>, Object>();
	}

	private IObjectCreator _creator;

	public ObjectFactory set(final IObjectCreator creator) {
		_creator = creator;
		return this;
	}

	private final Set<IObjectCreatorListener> listeners = new HashSet<IObjectCreatorListener>();

	public ObjectFactory addListener(final IObjectCreatorListener listener) {
		listeners.add(listener);
		return this;
	}

	@SuppressWarnings("unchecked")
	private <T> T _singleton(final Class<T> oClass) {
		if (oClass == null) {
			return null;
		}
		T o = (T) singletonCache.get(oClass);
		if (o == null) {
			o = _create(oClass);
			if (o != null) {
				singletonCache.put(oClass, o);
			}
		}
		return o;
	}

	private Object _singleton(final String className) {
		try {
			return _singleton(ClassUtils.forName(className));
		} catch (final ClassNotFoundException e) {
			throw ObjectInstanceException.of(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T _create(final Class<T> oClass) {
		try {
			final Class<?> nClass = original(oClass);
			for (final IObjectCreatorListener l : listeners) {
				l.onBefore(nClass);
			}
			if (_creator == null) {
				_creator = new IObjectCreator() {
					@Override
					public Object create(final Class<?> oClass) {
						return newInstance(oClass);
					}
				};
			}
			final T t = (T) _creator.create(nClass);
			for (final IObjectCreatorListener l : listeners) {
				l.onCreated(t);
			}
			return t;
		} catch (final Exception e) {
			throw ObjectInstanceException.of(e);
		}
	}

	private Object _create(final String className) {
		try {
			return _create(ClassUtils.forName(className));
		} catch (final ClassNotFoundException e) {
			throw ObjectInstanceException.of(e);
		}
	}

	private <T> T _newInstance(final Class<T> oClass) {
		try {
			return oClass.newInstance();
		} catch (final Exception e) {
			throw ObjectInstanceException.of(e);
		}
	}

	private Class<?> _original(final Class<?> proxy) {
		if (ProxyUtils.isProxy(proxy)) {
			return proxy.getSuperclass();
		}
		return proxy;
	}

	// private boolean isAbstract(final Class<?> oClass) {
	// int m;
	// return oClass == null || Modifier.isInterface(m = oClass.getModifiers())
	// || Modifier.isAbstract(m);
	// }

	public static interface IObjectCreatorListener {

		/**
		 * 创建前触发
		 * 
		 * @param oClass
		 */
		void onBefore(Class<?> oClass);

		/**
		 * 创建后触发
		 * 
		 * @param o
		 */
		void onCreated(Object o);
	}

	public static interface IObjectCreator {

		/**
		 * 创建
		 * 
		 * @param oClass
		 * @return
		 */
		Object create(Class<?> oClass);
	}
}

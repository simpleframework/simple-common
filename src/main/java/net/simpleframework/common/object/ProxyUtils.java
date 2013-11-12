package net.simpleframework.common.object;

import static net.simpleframework.common.I18n.$m;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.simpleframework.common.ClassUtils;
import net.simpleframework.common.coll.ArrayUtils;
import net.simpleframework.lib.net.sf.cglib.proxy.Enhancer;
import net.simpleframework.lib.net.sf.cglib.proxy.MethodInterceptor;
import net.simpleframework.lib.net.sf.cglib.proxy.MethodProxy;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public abstract class ProxyUtils {

	private static Map<Class<?>, Map<Class<?>, IMethodInterceptor>> registryCache;
	static {
		registryCache = new ConcurrentHashMap<Class<?>, Map<Class<?>, IMethodInterceptor>>();
	}

	public static void regist(final Class<?> oClass, final IMethodInterceptor interceptor) {
		Map<Class<?>, IMethodInterceptor> interceptors = registryCache.get(oClass);
		if (interceptors == null) {
			registryCache.put(oClass, interceptors = new HashMap<Class<?>, IMethodInterceptor>());
		}
		interceptors.put(interceptor.getClass(), interceptor);
	}

	public static boolean isRegistered(final Class<?> oClass,
			final Class<? extends IMethodInterceptor> iClass) {
		final Map<Class<?>, IMethodInterceptor> interceptors = registryCache.get(oClass);
		return interceptors != null && interceptors.containsKey(iClass);
	}

	public static Object create(final Class<?> oClass) {
		final Map<Class<?>, IMethodInterceptor> interceptors = registryCache.get(oClass);
		if (interceptors == null || interceptors.size() == 0) {
			return ObjectFactory.newInstance(oClass);
		}
		final Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(oClass);
		enhancer.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(final Object obj, final Method method, final Object[] args,
					final MethodProxy proxy) throws Throwable {
				for (final IMethodInterceptor interceptor : interceptors.values()) {
					final MethodResult ret = interceptor.intercept(obj, method, args, proxy);
					if (ret.isSuccess()) {
						return ret.getValue();
					}
				}
				return proxy.invokeSuper(obj, args);
			}
		});
		final Object o = enhancer.create();
		System.out.println($m("ProxyUtils.0", o.getClass().getName()));
		return o;
	}

	public static void registDefaults(final Class<?> oClass) {
		if (isRegistered(oClass, DefaultMethodInterceptor.class)) {
			return;
		}
		boolean mark = false;
		final Class<?>[] intfs = ClassUtils.getAllInterfaces(oClass);
		for (final Class<?> intf : intfs) {
			final MethodOverride mo = intf.getAnnotation(MethodOverride.class);
			if (mo != null) {
				mark = true;
				break;
			}
		}
		if (mark) {
			regist(oClass, new DefaultMethodInterceptor(intfs));
		}
	}

	static class DefaultMethodInterceptor implements IMethodInterceptor {
		private final List<Object[]> list = new ArrayList<Object[]>();

		DefaultMethodInterceptor(final Class<?>[] intfs) {
			for (final Class<?> intf : intfs) {
				final MethodOverride mo = intf.getAnnotation(MethodOverride.class);
				if (mo != null) {
					list.add(new Object[] { ObjectFactory.create(mo.impl()), mo.methods() });
				}
			}
		}

		@Override
		public MethodResult intercept(final Object obj, final Method method, final Object[] args,
				final MethodProxy proxy) throws Throwable {
			for (final Object[] arr : list) {
				final String[] methods = (String[]) arr[1];
				final String name = method.getName();
				if (ArrayUtils.contains(methods, name)) {
					final Object o = arr[0];
					Method nMethod;
					try {
						nMethod = o.getClass().getMethod(name, method.getParameterTypes());
					} catch (final NoSuchMethodException e) {
						nMethod = method;
						nMethod.setAccessible(true);
					}
					return new MethodResult(nMethod.invoke(o, args));
				}
			}
			return MethodResult.FAILURE;
		}
	}

	public static boolean isProxy(final Class<?> proxy) {
		return Enhancer.isEnhanced(proxy);
	}
}

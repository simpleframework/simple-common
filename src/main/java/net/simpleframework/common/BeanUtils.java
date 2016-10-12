package net.simpleframework.common;

import static java.util.Locale.ENGLISH;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.logger.Log;
import net.simpleframework.common.logger.LogFactory;
import net.simpleframework.common.object.ObjectFactory;
import net.simpleframework.common.th.ClassException;
import net.simpleframework.lib.net.sf.cglib.beans.BeanMap;
import net.simpleframework.lib.net.sf.cglib.beans.BeanMap.Generator;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class BeanUtils {
	static Log log = LogFactory.getLogger(BeanUtils.class);

	private static Map<Class<?>, BeanWrapper> wrapperCache;
	static {
		wrapperCache = new ConcurrentHashMap<Class<?>, BeanWrapper>();
	}

	private static BeanWrapper getBeanWrapper(final Class<?> beanClass) {
		BeanWrapper wrapper = wrapperCache.get(beanClass);
		if (wrapper != null) {
			return wrapper;
		}

		wrapper = new BeanWrapper();

		// beanMap
		final Generator gen = new Generator();
		gen.setBeanClass(beanClass);
		wrapper.beanMap = gen.create();

		// properties
		PropertyDescriptor[] arr;
		try {
			arr = Introspector.getBeanInfo(beanClass, Object.class).getPropertyDescriptors();
		} catch (final IntrospectionException e) {
			throw ClassException.of(e);
		}

		final Set<String> keys = wrapper.beanMap.keySet();
		wrapper.properties = new HashMap<String, PropertyWrapper>();
		for (final PropertyDescriptor pd : arr) {
			final String name = pd.getName();
			if (!keys.contains(name)) {
				continue;
			}

			final PropertyWrapper property = new PropertyWrapper();
			property.name = name;
			property.getter = pd.getReadMethod();
			property.setter = pd.getWriteMethod();
			property.type = pd.getPropertyType();
			if (property.type == Enum.class) {
				property.type = property.getter.getReturnType();
			}

			Class<?> nextClass = beanClass;
			while (nextClass != Object.class) {
				try {
					property.field = nextClass.getDeclaredField(name);
					break;
				} catch (final NoSuchFieldException e) {
				}
				nextClass = nextClass.getSuperclass();
			}

			if (property.setter == null) {
				try {
					// 由于一些setter方法返回this，不符合规范
					property.setter2 = beanClass.getMethod(getSetterName(name), property.type);
				} catch (final NoSuchMethodException e) {
				}
			}

			wrapper.properties.put(name, property);
		}

		wrapperCache.put(beanClass, wrapper);
		return wrapper;
	}

	public static Map<String, PropertyWrapper> getProperties(final Class<?> beanClass) {
		return new HashMap(getBeanWrapper(beanClass).properties);
	}

	public static boolean hasProperty(final Object bean, final String name) {
		if (bean instanceof Map) {
			return ((Map) bean).containsKey(name);
		} else {
			if (bean == null) {
				return false;
			}
			final BeanWrapper wrapper = getBeanWrapper(
					(bean instanceof Class) ? (Class<?>) bean : bean.getClass());
			return wrapper.containsKey(name);
		}
	}

	public static Class<?> getPropertyType(final Object bean, final String name) {
		if (bean instanceof Map) {
			final Object o = ((Map) bean).get(name);
			return o != null ? o.getClass() : null;
		} else {
			if (bean == null) {
				return null;
			}
			final BeanWrapper wrapper = getBeanWrapper(
					(bean instanceof Class) ? (Class<?>) bean : bean.getClass());
			return wrapper.getPropertyType(name);
		}
	}

	/**
	 * 获取bean的属性值。取消了反射，采用字节码操作
	 * 
	 * @param bean
	 * @param name
	 * @return
	 */
	public static Object getProperty(final Object bean, final String name) {
		if (bean instanceof Map) {
			return ((Map) bean).get(name);
		} else {
			if (bean == null) {
				return null;
			}
			return getBeanWrapper(bean.getClass()).get(bean, name);
		}
	}

	public static void setProperty(final Object bean, final String name, final Object value) {
		if (bean instanceof Map) {
			((Map) bean).put(name, value);
		} else {
			if (bean != null) {
				getBeanWrapper(bean.getClass()).set(bean, name, value);
			}
		}
	}

	public static void setProperties(final Object bean, final Map<String, Object> properties) {
		for (final Map.Entry<String, Object> e : properties.entrySet()) {
			setProperty(bean, e.getKey(), e.getValue());
		}
	}

	public static Map<String, Object> toMap(final Object bean) {
		return toMap(bean, true);
	}

	public static Map<String, Object> toMap(final Object bean, final boolean caseInsensitive) {
		final KVMap kv = new KVMap().setCaseInsensitive(caseInsensitive);
		if (bean != null) {
			getBeanWrapper(bean.getClass()).copy(bean, kv);
		}
		return kv;
	}

	public static <T> T toBean(final Class<T> tClass, final Map<String, Object> map) {
		final T t = ObjectFactory.newInstance(tClass);
		getBeanWrapper(tClass).copy(map, t);
		return t;
	}

	public static <T> T clone(final T t) {
		final Class<?> tClass = t.getClass();
		final T o = (T) ObjectFactory.newInstance(tClass);
		getBeanWrapper(tClass).clone(t, o);
		return o;
	}

	public static String getSetterName(final String key) {
		return "set" + key.substring(0, 1).toUpperCase(ENGLISH) + key.substring(1);
	}

	private static Object convert(final Object value, final Class<?> parameterType) {
		if (ID.class.isAssignableFrom(parameterType)) {
			return ID.of(value);
		} else if (Version.class.isAssignableFrom(parameterType)) {
			return Version.getVersion(String.valueOf(value));
		}
		return Convert.convert(value, parameterType);
	}

	public static class PropertyWrapper {
		public String name;
		public Field field;
		public Method getter, setter;
		public Method setter2;
		public Class<?> type;
	}

	private static class BeanWrapper {
		Map<String, PropertyWrapper> properties;

		BeanMap beanMap;

		boolean containsKey(final String key) {
			return properties.containsKey(key);
		}

		Class<?> getPropertyType(final String key) {
			final PropertyWrapper pw = properties.get(key);
			return pw != null ? pw.type : null;
		}

		void set(final Object bean, final String key, final Object val) {
			final PropertyWrapper pw = properties.get(key);
			if (pw != null) {
				final Object val2 = convert(val, pw.type);
				if (val2 == null && pw.type.isPrimitive()) {
					// 基本类型忽略
					return;
				}
				if (pw.setter != null) {
					beanMap.put(bean, key, val2);
				} else if (pw.setter2 != null) {
					try {
						// 处理不符合bean规范的
						pw.setter2.invoke(bean, val2);
					} catch (final Exception e) {
						log.warn(e);
					}
				}
			}
		}

		Object get(final Object bean, final String key) {
			return beanMap.get(bean, key);
		}

		void copy(final Object bean, final Map<String, Object> map) {
			for (final String key : properties.keySet()) {
				map.put(key, get(bean, key));
			}
		}

		void copy(final Map<String, Object> map, final Object bean) {
			for (final String key : properties.keySet()) {
				final Object val = map.get(key);
				if (val != null) {
					set(bean, key, val);
				}
			}
		}

		<T> void clone(final T bean, final T bean2) {
			for (final String key : properties.keySet()) {
				final Object val = get(bean, key);
				if (val != null) {
					set(bean2, key, val);
				}
			}
		}
	}
}

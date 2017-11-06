package net.simpleframework.lib.net.minidev.asm;

/*
 * Copyright 2011 JSON-SMART authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allow access reflect field using runtime generated accessor. BeansAccessor is
 * faster than java.lang.reflect.Method.invoke()
 * 
 * @author uriel Chemouni
 */
public abstract class BeansAccess<T> {
	private HashMap<String, Accessor> map;
	private Accessor[] accs;

	protected void setAccessor(final Accessor[] accs) {
		int i = 0;
		this.accs = accs;
		map = new HashMap<>();
		for (final Accessor acc : accs) {
			acc.index = i++;
			map.put(acc.getName(), acc);
		}
	}

	public HashMap<String, Accessor> getMap() {
		return map;
	}

	public Accessor[] getAccessors() {
		return accs;
	}

	/**
	 * cache used to store built BeansAccess
	 */
	private static ConcurrentHashMap<Class<?>, BeansAccess<?>> cache = new ConcurrentHashMap<>();

	// private final static ConcurrentHashMap<Type, AMapper<?>> cache;

	/**
	 * return the BeansAccess corresponding to a type
	 * 
	 * @param type
	 *        to be access
	 * @return the BeansAccess
	 */
	static public <P> BeansAccess<P> get(final Class<P> type) {
		return get(type, null);
	}

	/**
	 * return the BeansAccess corresponding to a type
	 * 
	 * @param type
	 *        to be access
	 * @return the BeansAccess
	 */
	static public <P> BeansAccess<P> get(final Class<P> type, final FieldFilter filter) {
		{
			@SuppressWarnings("unchecked")
			final BeansAccess<P> access = (BeansAccess<P>) cache.get(type);
			if (access != null) {
				return access;
			}
		}
		// extract all access methodes
		final Accessor[] accs = ASMUtil.getAccessors(type, filter);

		// create new class name
		final String className = type.getName();
		String accessClassName;
		if (className.startsWith("java.util.")) {
			accessClassName = "net.simpleframework.lib.net.minidev.asm." + className + "AccAccess";
		} else {
			accessClassName = className.concat("AccAccess");
		}

		// extend class base loader
		final DynamicClassLoader loader = new DynamicClassLoader(type.getClassLoader());
		// try to load existing class
		Class<?> accessClass = null;
		try {
			accessClass = loader.loadClass(accessClassName);
		} catch (final ClassNotFoundException ignored) {
		}

		final LinkedList<Class<?>> parentClasses = getParents(type);

		// if the class do not exists build it
		if (accessClass == null) {
			final BeansAccessBuilder builder = new BeansAccessBuilder(type, accs, loader);
			for (final Class<?> c : parentClasses) {
				builder.addConversion(BeansAccessConfig.classMapper.get(c));
			}
			accessClass = builder.bulid();
		}
		try {
			@SuppressWarnings("unchecked")
			final BeansAccess<P> access = (BeansAccess<P>) accessClass.newInstance();
			access.setAccessor(accs);
			cache.putIfAbsent(type, access);
			// add fieldname alias
			for (final Class<?> c : parentClasses) {
				addAlias(access, BeansAccessConfig.classFiledNameMapper.get(c));
			}
			return access;
		} catch (final Exception ex) {
			throw new RuntimeException("Error constructing accessor class: " + accessClassName, ex);
		}
	}

	private static LinkedList<Class<?>> getParents(Class<?> type) {
		final LinkedList<Class<?>> m = new LinkedList<>();
		while (type != null && !type.equals(Object.class)) {
			m.addLast(type);
			for (final Class<?> c : type.getInterfaces()) {
				m.addLast(c);
			}
			type = type.getSuperclass();
		}
		m.addLast(Object.class);
		return m;
	}

	/**
	 * 
	 */
	private static void addAlias(final BeansAccess<?> access, final HashMap<String, String> m) {
		// HashMap<String, String> m =
		// BeansAccessConfig.classFiledNameMapper.get(type);
		if (m == null) {
			return;
		}
		final HashMap<String, Accessor> changes = new HashMap<>();
		for (final Entry<String, String> e : m.entrySet()) {
			final Accessor a1 = access.map.get(e.getValue());
			if (a1 != null) {
				changes.put(e.getValue(), a1);
			}
		}
		access.map.putAll(changes);
	}

	/**
	 * set field value by field index
	 */
	abstract public void set(T object, int methodIndex, Object value);

	/**
	 * get field value by field index
	 */
	abstract public Object get(T object, int methodIndex);

	/**
	 * create a new targeted object
	 */
	abstract public T newInstance();

	/**
	 * set field value by fieldname
	 */
	public void set(final T object, final String methodName, final Object value) {
		final int i = getIndex(methodName);
		if (i == -1) {
			throw new net.simpleframework.lib.net.minidev.asm.ex.NoSuchFieldException(
					methodName + " in " + object.getClass() + " to put value : " + value);
		}
		set(object, i, value);
	}

	/**
	 * get field value by fieldname
	 */
	public Object get(final T object, final String methodName) {
		return get(object, getIndex(methodName));
	}

	/**
	 * Returns the index of the field accessor.
	 */
	public int getIndex(final String name) {
		final Accessor ac = map.get(name);
		if (ac == null) {
			return -1;
		}
		return ac.index;
	}
}

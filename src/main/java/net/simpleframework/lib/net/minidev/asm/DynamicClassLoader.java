package net.simpleframework.lib.net.minidev.asm;

/*
 *    Copyright 2011 JSON-SMART authors
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
import java.lang.reflect.Method;

/**
 * Simple extension from ClassLoader overiding the loadClass(String name,
 * boolean resolve) method and allowing to register new classes
 * 
 * @author uriel
 * 
 */
class DynamicClassLoader extends ClassLoader {
	DynamicClassLoader(final ClassLoader parent) {
		super(parent);
	}

	private final static String BEAN_AC = BeansAccess.class.getName();
	/**
	 * Predefined define defineClass method signature (name, bytes, offset,
	 * length)
	 */
	private final static Class<?>[] DEF_CLASS_SIG = new Class[] { String.class, byte[].class,
			int.class, int.class };

	public static <T> Class<T> directLoad(final Class<? extends T> parent, final String clsName,
			final byte[] clsData) {
		final DynamicClassLoader loader = new DynamicClassLoader(parent.getClassLoader());
		@SuppressWarnings("unchecked")
		final Class<T> clzz = (Class<T>) loader.defineClass(clsName, clsData);
		return clzz;
	}

	public static <T> T directInstance(final Class<? extends T> parent, final String clsName,
			final byte[] clsData) throws InstantiationException, IllegalAccessException {
		final Class<T> clzz = directLoad(parent, clsName, clsData);
		return clzz.newInstance();
	}

	@Override
	protected synchronized java.lang.Class<?> loadClass(final String name, final boolean resolve)
			throws ClassNotFoundException {
		/*
		 * check class by fullname as String.
		 */
		if (name.equals(BEAN_AC)) {
			return BeansAccess.class;
		}
		/*
		 * Use default class loader
		 */
		return super.loadClass(name, resolve);
	}

	/**
	 * Call defineClass into the parent classLoader using the
	 * method.setAccessible(boolean) hack
	 * 
	 * @see ClassLoader#defineClass(String, byte[], int, int)
	 */
	Class<?> defineClass(final String name, final byte[] bytes) throws ClassFormatError {
		try {
			// Attempt to load the access class in the same loader, which makes
			// protected and default access members accessible.
			final Method method = ClassLoader.class.getDeclaredMethod("defineClass", DEF_CLASS_SIG);
			method.setAccessible(true);
			return (Class<?>) method.invoke(getParent(),
					new Object[] { name, bytes, Integer.valueOf(0), Integer.valueOf(bytes.length) });
		} catch (final Exception ignored) {
		}
		return defineClass(name, bytes, 0, bytes.length);
	}
}

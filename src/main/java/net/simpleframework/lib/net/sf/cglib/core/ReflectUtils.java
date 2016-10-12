/*
 * Copyright 2003,2004 The Apache Software Foundation
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
package net.simpleframework.lib.net.sf.cglib.core;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.simpleframework.lib.org.objectweb.asm.Attribute;
import net.simpleframework.lib.org.objectweb.asm.Type;

/**
 * @version $Id: ReflectUtils.java,v 1.30 2009/01/11 19:47:49 herbyderby Exp $
 */
public class ReflectUtils {
	private ReflectUtils() {
	}

	private static final Map primitives = new HashMap(8);
	private static final Map transforms = new HashMap(8);
	private static final ClassLoader defaultLoader = ReflectUtils.class.getClassLoader();
	private static Method DEFINE_CLASS;
	private static final ProtectionDomain PROTECTION_DOMAIN;

	private static final List<Method> OBJECT_METHODS = new ArrayList<Method>();

	static {
		PROTECTION_DOMAIN = getProtectionDomain(ReflectUtils.class);

		AccessController.doPrivileged(new PrivilegedAction() {
			@Override
			public Object run() {
				try {
					final Class loader = Class.forName("java.lang.ClassLoader"); // JVM
					// crash
					// w/o
					// this
					DEFINE_CLASS = loader.getDeclaredMethod("defineClass", new Class[] { String.class,
							byte[].class, Integer.TYPE, Integer.TYPE, ProtectionDomain.class });
					DEFINE_CLASS.setAccessible(true);
				} catch (final ClassNotFoundException e) {
					throw new CodeGenerationException(e);
				} catch (final NoSuchMethodException e) {
					throw new CodeGenerationException(e);
				}
				return null;
			}
		});
		final Method[] methods = Object.class.getDeclaredMethods();
		for (final Method method : methods) {
			if ("finalize".equals(method.getName())
					|| (method.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) > 0) {
				continue;
			}
			OBJECT_METHODS.add(method);
		}
	}

	private static final String[] CGLIB_PACKAGES = { "java.lang", };

	static {
		primitives.put("byte", Byte.TYPE);
		primitives.put("char", Character.TYPE);
		primitives.put("double", Double.TYPE);
		primitives.put("float", Float.TYPE);
		primitives.put("int", Integer.TYPE);
		primitives.put("long", Long.TYPE);
		primitives.put("short", Short.TYPE);
		primitives.put("boolean", Boolean.TYPE);

		transforms.put("byte", "B");
		transforms.put("char", "C");
		transforms.put("double", "D");
		transforms.put("float", "F");
		transforms.put("int", "I");
		transforms.put("long", "J");
		transforms.put("short", "S");
		transforms.put("boolean", "Z");
	}

	public static ProtectionDomain getProtectionDomain(final Class source) {
		if (source == null) {
			return null;
		}
		return (ProtectionDomain) AccessController.doPrivileged(new PrivilegedAction() {
			@Override
			public Object run() {
				return source.getProtectionDomain();
			}
		});
	}

	public static Type[] getExceptionTypes(final Member member) {
		if (member instanceof Method) {
			return TypeUtils.getTypes(((Method) member).getExceptionTypes());
		} else if (member instanceof Constructor) {
			return TypeUtils.getTypes(((Constructor) member).getExceptionTypes());
		} else {
			throw new IllegalArgumentException("Cannot get exception types of a field");
		}
	}

	public static Signature getSignature(final Member member) {
		if (member instanceof Method) {
			return new Signature(member.getName(), Type.getMethodDescriptor((Method) member));
		} else if (member instanceof Constructor) {
			final Type[] types = TypeUtils.getTypes(((Constructor) member).getParameterTypes());
			return new Signature(Constants.CONSTRUCTOR_NAME,
					Type.getMethodDescriptor(Type.VOID_TYPE, types));

		} else {
			throw new IllegalArgumentException("Cannot get signature of a field");
		}
	}

	public static Constructor findConstructor(final String desc) {
		return findConstructor(desc, defaultLoader);
	}

	public static Constructor findConstructor(final String desc, final ClassLoader loader) {
		try {
			final int lparen = desc.indexOf('(');
			final String className = desc.substring(0, lparen).trim();
			return getClass(className, loader).getConstructor(parseTypes(desc, loader));
		} catch (final ClassNotFoundException e) {
			throw new CodeGenerationException(e);
		} catch (final NoSuchMethodException e) {
			throw new CodeGenerationException(e);
		}
	}

	public static Method findMethod(final String desc) {
		return findMethod(desc, defaultLoader);
	}

	public static Method findMethod(final String desc, final ClassLoader loader) {
		try {
			final int lparen = desc.indexOf('(');
			final int dot = desc.lastIndexOf('.', lparen);
			final String className = desc.substring(0, dot).trim();
			final String methodName = desc.substring(dot + 1, lparen).trim();
			return getClass(className, loader).getDeclaredMethod(methodName, parseTypes(desc, loader));
		} catch (final ClassNotFoundException e) {
			throw new CodeGenerationException(e);
		} catch (final NoSuchMethodException e) {
			throw new CodeGenerationException(e);
		}
	}

	private static Class[] parseTypes(final String desc, final ClassLoader loader)
			throws ClassNotFoundException {
		final int lparen = desc.indexOf('(');
		final int rparen = desc.indexOf(')', lparen);
		final List params = new ArrayList();
		int start = lparen + 1;
		for (;;) {
			final int comma = desc.indexOf(',', start);
			if (comma < 0) {
				break;
			}
			params.add(desc.substring(start, comma).trim());
			start = comma + 1;
		}
		if (start < rparen) {
			params.add(desc.substring(start, rparen).trim());
		}
		final Class[] types = new Class[params.size()];
		for (int i = 0; i < types.length; i++) {
			types[i] = getClass((String) params.get(i), loader);
		}
		return types;
	}

	private static Class getClass(final String className, final ClassLoader loader)
			throws ClassNotFoundException {
		return getClass(className, loader, CGLIB_PACKAGES);
	}

	private static Class getClass(String className, final ClassLoader loader,
			final String[] packages) throws ClassNotFoundException {
		final String save = className;
		int dimensions = 0;
		int index = 0;
		while ((index = className.indexOf("[]", index) + 1) > 0) {
			dimensions++;
		}
		final StringBuffer brackets = new StringBuffer(className.length() - dimensions);
		for (int i = 0; i < dimensions; i++) {
			brackets.append('[');
		}
		className = className.substring(0, className.length() - 2 * dimensions);

		final String prefix = (dimensions > 0) ? brackets + "L" : "";
		final String suffix = (dimensions > 0) ? ";" : "";
		try {
			return Class.forName(prefix + className + suffix, false, loader);
		} catch (final ClassNotFoundException ignore) {
		}
		for (int i = 0; i < packages.length; i++) {
			try {
				return Class.forName(prefix + packages[i] + '.' + className + suffix, false, loader);
			} catch (final ClassNotFoundException ignore) {
			}
		}
		if (dimensions == 0) {
			final Class c = (Class) primitives.get(className);
			if (c != null) {
				return c;
			}
		} else {
			final String transform = (String) transforms.get(className);
			if (transform != null) {
				try {
					return Class.forName(brackets + transform, false, loader);
				} catch (final ClassNotFoundException ignore) {
				}
			}
		}
		throw new ClassNotFoundException(save);
	}

	public static Object newInstance(final Class type) {
		return newInstance(type, Constants.EMPTY_CLASS_ARRAY, null);
	}

	public static Object newInstance(final Class type, final Class[] parameterTypes,
			final Object[] args) {
		return newInstance(getConstructor(type, parameterTypes), args);
	}

	public static Object newInstance(final Constructor cstruct, final Object[] args) {

		final boolean flag = cstruct.isAccessible();
		try {
			cstruct.setAccessible(true);
			final Object result = cstruct.newInstance(args);
			return result;
		} catch (final InstantiationException e) {
			throw new CodeGenerationException(e);
		} catch (final IllegalAccessException e) {
			throw new CodeGenerationException(e);
		} catch (final InvocationTargetException e) {
			throw new CodeGenerationException(e.getTargetException());
		} finally {
			cstruct.setAccessible(flag);
		}

	}

	public static Constructor getConstructor(final Class type, final Class[] parameterTypes) {
		try {
			final Constructor constructor = type.getDeclaredConstructor(parameterTypes);
			constructor.setAccessible(true);
			return constructor;
		} catch (final NoSuchMethodException e) {
			throw new CodeGenerationException(e);
		}
	}

	public static String[] getNames(final Class[] classes) {
		if (classes == null) {
			return null;
		}
		final String[] names = new String[classes.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = classes[i].getName();
		}
		return names;
	}

	public static Class[] getClasses(final Object[] objects) {
		final Class[] classes = new Class[objects.length];
		for (int i = 0; i < objects.length; i++) {
			classes[i] = objects[i].getClass();
		}
		return classes;
	}

	public static Method findNewInstance(final Class iface) {
		final Method m = findInterfaceMethod(iface);
		if (!m.getName().equals("newInstance")) {
			throw new IllegalArgumentException(iface + " missing newInstance method");
		}
		return m;
	}

	public static Method[] getPropertyMethods(final PropertyDescriptor[] properties,
			final boolean read, final boolean write) {
		final Set methods = new HashSet();
		for (int i = 0; i < properties.length; i++) {
			final PropertyDescriptor pd = properties[i];
			if (read) {
				methods.add(pd.getReadMethod());
			}
			if (write) {
				methods.add(pd.getWriteMethod());
			}
		}
		methods.remove(null);
		return (Method[]) methods.toArray(new Method[methods.size()]);
	}

	public static PropertyDescriptor[] getBeanProperties(final Class type) {
		return getPropertiesHelper(type, true, true);
	}

	public static PropertyDescriptor[] getBeanGetters(final Class type) {
		return getPropertiesHelper(type, true, false);
	}

	public static PropertyDescriptor[] getBeanSetters(final Class type) {
		return getPropertiesHelper(type, false, true);
	}

	private static PropertyDescriptor[] getPropertiesHelper(final Class type, final boolean read,
			final boolean write) {
		try {
			final BeanInfo info = Introspector.getBeanInfo(type, Object.class);
			final PropertyDescriptor[] all = info.getPropertyDescriptors();
			if (read && write) {
				return all;
			}
			final List properties = new ArrayList(all.length);
			for (int i = 0; i < all.length; i++) {
				final PropertyDescriptor pd = all[i];
				if ((read && pd.getReadMethod() != null) || (write && pd.getWriteMethod() != null)) {
					properties.add(pd);
				}
			}
			return (PropertyDescriptor[]) properties
					.toArray(new PropertyDescriptor[properties.size()]);
		} catch (final IntrospectionException e) {
			throw new CodeGenerationException(e);
		}
	}

	public static Method findDeclaredMethod(final Class type, final String methodName,
			final Class[] parameterTypes) throws NoSuchMethodException {

		Class cl = type;
		while (cl != null) {
			try {
				return cl.getDeclaredMethod(methodName, parameterTypes);
			} catch (final NoSuchMethodException e) {
				cl = cl.getSuperclass();
			}
		}
		throw new NoSuchMethodException(methodName);

	}

	public static List addAllMethods(final Class type, final List list) {

		if (type == Object.class) {
			list.addAll(OBJECT_METHODS);
		} else {
			list.addAll(java.util.Arrays.asList(type.getDeclaredMethods()));
		}

		final Class superclass = type.getSuperclass();
		if (superclass != null) {
			addAllMethods(superclass, list);
		}
		final Class[] interfaces = type.getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			addAllMethods(interfaces[i], list);
		}

		return list;
	}

	public static List addAllInterfaces(final Class type, final List list) {
		final Class superclass = type.getSuperclass();
		if (superclass != null) {
			list.addAll(Arrays.asList(type.getInterfaces()));
			addAllInterfaces(superclass, list);
		}
		return list;
	}

	public static Method findInterfaceMethod(final Class iface) {
		if (!iface.isInterface()) {
			throw new IllegalArgumentException(iface + " is not an interface");
		}
		final Method[] methods = iface.getDeclaredMethods();
		if (methods.length != 1) {
			throw new IllegalArgumentException("expecting exactly 1 method in " + iface);
		}
		return methods[0];
	}

	public static Class defineClass(final String className, final byte[] b, final ClassLoader loader)
			throws Exception {
		return defineClass(className, b, loader, PROTECTION_DOMAIN);
	}

	public static Class defineClass(final String className, final byte[] b, final ClassLoader loader,
			final ProtectionDomain protectionDomain) throws Exception {
		final Object[] args = new Object[] { className, b, new Integer(0), new Integer(b.length),
				protectionDomain };
		final Class c = (Class) DEFINE_CLASS.invoke(loader, args);
		// Force static initializers to run.
		Class.forName(className, true, loader);
		return c;
	}

	public static int findPackageProtected(final Class[] classes) {
		for (int i = 0; i < classes.length; i++) {
			if (!Modifier.isPublic(classes[i].getModifiers())) {
				return i;
			}
		}
		return 0;
	}

	public static MethodInfo getMethodInfo(final Member member, final int modifiers) {
		final Signature sig = getSignature(member);
		return new MethodInfo() {
			private ClassInfo ci;

			@Override
			public ClassInfo getClassInfo() {
				if (ci == null) {
					ci = ReflectUtils.getClassInfo(member.getDeclaringClass());
				}
				return ci;
			}

			@Override
			public int getModifiers() {
				return modifiers;
			}

			@Override
			public Signature getSignature() {
				return sig;
			}

			@Override
			public Type[] getExceptionTypes() {
				return ReflectUtils.getExceptionTypes(member);
			}

			public Attribute getAttribute() {
				return null;
			}
		};
	}

	public static MethodInfo getMethodInfo(final Member member) {
		return getMethodInfo(member, member.getModifiers());
	}

	public static ClassInfo getClassInfo(final Class clazz) {
		final Type type = Type.getType(clazz);
		final Type sc = (clazz.getSuperclass() == null) ? null : Type.getType(clazz.getSuperclass());
		return new ClassInfo() {
			@Override
			public Type getType() {
				return type;
			}

			@Override
			public Type getSuperType() {
				return sc;
			}

			@Override
			public Type[] getInterfaces() {
				return TypeUtils.getTypes(clazz.getInterfaces());
			}

			@Override
			public int getModifiers() {
				return clazz.getModifiers();
			}
		};
	}

	// used by MethodInterceptorGenerated generated code
	public static Method[] findMethods(final String[] namesAndDescriptors, final Method[] methods) {
		final Map map = new HashMap();
		for (int i = 0; i < methods.length; i++) {
			final Method method = methods[i];
			map.put(method.getName() + Type.getMethodDescriptor(method), method);
		}
		final Method[] result = new Method[namesAndDescriptors.length / 2];
		for (int i = 0; i < result.length; i++) {
			result[i] = (Method) map.get(namesAndDescriptors[i * 2] + namesAndDescriptors[i * 2 + 1]);
			if (result[i] == null) {
				// TODO: error?
			}
		}
		return result;
	}
}

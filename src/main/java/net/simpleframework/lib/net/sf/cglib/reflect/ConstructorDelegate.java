/*
 * Copyright 2003 The Apache Software Foundation
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
package net.simpleframework.lib.net.sf.cglib.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

import net.simpleframework.lib.net.sf.cglib.core.AbstractClassGenerator;
import net.simpleframework.lib.net.sf.cglib.core.ClassEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.EmitUtils;
import net.simpleframework.lib.net.sf.cglib.core.KeyFactory;
import net.simpleframework.lib.net.sf.cglib.core.ReflectUtils;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

/**
 * @author Chris Nokleberg
 * @version $Id: ConstructorDelegate.java,v 1.20 2006/03/05 02:43:19 herbyderby
 *          Exp $
 */
abstract public class ConstructorDelegate {
	private static final ConstructorKey KEY_FACTORY = (ConstructorKey) KeyFactory
			.create(ConstructorKey.class, KeyFactory.CLASS_BY_NAME);

	interface ConstructorKey {
		public Object newInstance(String declaring, String iface);
	}

	protected ConstructorDelegate() {
	}

	public static ConstructorDelegate create(final Class targetClass, final Class iface) {
		final Generator gen = new Generator();
		gen.setTargetClass(targetClass);
		gen.setInterface(iface);
		return gen.create();
	}

	public static class Generator extends AbstractClassGenerator {
		private static final Source SOURCE = new Source(ConstructorDelegate.class.getName());
		private static final Type CONSTRUCTOR_DELEGATE = TypeUtils
				.parseType("net.simpleframework.lib.net.sf.cglib.reflect.ConstructorDelegate");

		private Class iface;
		private Class targetClass;

		public Generator() {
			super(SOURCE);
		}

		public void setInterface(final Class iface) {
			this.iface = iface;
		}

		public void setTargetClass(final Class targetClass) {
			this.targetClass = targetClass;
		}

		public ConstructorDelegate create() {
			setNamePrefix(targetClass.getName());
			final Object key = KEY_FACTORY.newInstance(iface.getName(), targetClass.getName());
			return (ConstructorDelegate) super.create(key);
		}

		@Override
		protected ClassLoader getDefaultClassLoader() {
			return targetClass.getClassLoader();
		}

		@Override
		protected ProtectionDomain getProtectionDomain() {
			return ReflectUtils.getProtectionDomain(targetClass);
		}

		@Override
		public void generateClass(final ClassVisitor v) {
			setNamePrefix(targetClass.getName());

			final Method newInstance = ReflectUtils.findNewInstance(iface);
			if (!newInstance.getReturnType().isAssignableFrom(targetClass)) {
				throw new IllegalArgumentException("incompatible return type");
			}
			final Constructor constructor;
			try {
				constructor = targetClass.getDeclaredConstructor(newInstance.getParameterTypes());
			} catch (final NoSuchMethodException e) {
				throw new IllegalArgumentException("interface does not match any known constructor");
			}

			final ClassEmitter ce = new ClassEmitter(v);
			ce.begin_class(Opcodes.V1_2, Opcodes.ACC_PUBLIC, getClassName(), CONSTRUCTOR_DELEGATE,
					new Type[] { Type.getType(iface) }, Constants.SOURCE_FILE);
			final Type declaring = Type.getType(constructor.getDeclaringClass());
			EmitUtils.null_constructor(ce);
			final CodeEmitter e = ce.begin_method(Opcodes.ACC_PUBLIC,
					ReflectUtils.getSignature(newInstance), ReflectUtils.getExceptionTypes(newInstance));
			e.new_instance(declaring);
			e.dup();
			e.load_args();
			e.invoke_constructor(declaring, ReflectUtils.getSignature(constructor));
			e.return_value();
			e.end_method();
			ce.end_class();
		}

		@Override
		protected Object firstInstance(final Class type) {
			return ReflectUtils.newInstance(type);
		}

		@Override
		protected Object nextInstance(final Object instance) {
			return instance;
		}
	}
}

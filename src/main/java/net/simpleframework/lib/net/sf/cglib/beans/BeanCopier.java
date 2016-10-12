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
package net.simpleframework.lib.net.sf.cglib.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import net.simpleframework.lib.net.sf.cglib.core.AbstractClassGenerator;
import net.simpleframework.lib.net.sf.cglib.core.ClassEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.Converter;
import net.simpleframework.lib.net.sf.cglib.core.EmitUtils;
import net.simpleframework.lib.net.sf.cglib.core.KeyFactory;
import net.simpleframework.lib.net.sf.cglib.core.Local;
import net.simpleframework.lib.net.sf.cglib.core.MethodInfo;
import net.simpleframework.lib.net.sf.cglib.core.ReflectUtils;
import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

/**
 * @author Chris Nokleberg
 */
abstract public class BeanCopier {
	private static final BeanCopierKey KEY_FACTORY = (BeanCopierKey) KeyFactory
			.create(BeanCopierKey.class);
	private static final Type CONVERTER = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.core.Converter");
	private static final Type BEAN_COPIER = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.beans.BeanCopier");
	private static final Signature COPY = new Signature("copy", Type.VOID_TYPE,
			new Type[] { Constants.TYPE_OBJECT, Constants.TYPE_OBJECT, CONVERTER });
	private static final Signature CONVERT = TypeUtils
			.parseSignature("Object convert(Object, Class, Object)");

	interface BeanCopierKey {
		public Object newInstance(String source, String target, boolean useConverter);
	}

	public static BeanCopier create(final Class source, final Class target,
			final boolean useConverter) {
		final Generator gen = new Generator();
		gen.setSource(source);
		gen.setTarget(target);
		gen.setUseConverter(useConverter);
		return gen.create();
	}

	abstract public void copy(Object from, Object to, Converter converter);

	public static class Generator extends AbstractClassGenerator {
		private static final Source SOURCE = new Source(BeanCopier.class.getName());
		private Class source;
		private Class target;
		private boolean useConverter;

		public Generator() {
			super(SOURCE);
		}

		public void setSource(final Class source) {
			if (!Modifier.isPublic(source.getModifiers())) {
				setNamePrefix(source.getName());
			}
			this.source = source;
		}

		public void setTarget(final Class target) {
			if (!Modifier.isPublic(target.getModifiers())) {
				setNamePrefix(target.getName());
			}

			this.target = target;
		}

		public void setUseConverter(final boolean useConverter) {
			this.useConverter = useConverter;
		}

		@Override
		protected ClassLoader getDefaultClassLoader() {
			return source.getClassLoader();
		}

		@Override
		protected ProtectionDomain getProtectionDomain() {
			return ReflectUtils.getProtectionDomain(source);
		}

		public BeanCopier create() {
			final Object key = KEY_FACTORY.newInstance(source.getName(), target.getName(),
					useConverter);
			return (BeanCopier) super.create(key);
		}

		@Override
		public void generateClass(final ClassVisitor v) {
			final Type sourceType = Type.getType(source);
			final Type targetType = Type.getType(target);
			final ClassEmitter ce = new ClassEmitter(v);
			ce.begin_class(Opcodes.V1_2, Opcodes.ACC_PUBLIC, getClassName(), BEAN_COPIER, null,
					Constants.SOURCE_FILE);

			EmitUtils.null_constructor(ce);
			final CodeEmitter e = ce.begin_method(Opcodes.ACC_PUBLIC, COPY, null);
			final PropertyDescriptor[] getters = ReflectUtils.getBeanGetters(source);
			final PropertyDescriptor[] setters = ReflectUtils.getBeanSetters(target);

			final Map names = new HashMap();
			for (int i = 0; i < getters.length; i++) {
				names.put(getters[i].getName(), getters[i]);
			}
			final Local targetLocal = e.make_local();
			final Local sourceLocal = e.make_local();
			if (useConverter) {
				e.load_arg(1);
				e.checkcast(targetType);
				e.store_local(targetLocal);
				e.load_arg(0);
				e.checkcast(sourceType);
				e.store_local(sourceLocal);
			} else {
				e.load_arg(1);
				e.checkcast(targetType);
				e.load_arg(0);
				e.checkcast(sourceType);
			}
			for (int i = 0; i < setters.length; i++) {
				final PropertyDescriptor setter = setters[i];
				final PropertyDescriptor getter = (PropertyDescriptor) names.get(setter.getName());
				if (getter != null) {
					final MethodInfo read = ReflectUtils.getMethodInfo(getter.getReadMethod());
					final MethodInfo write = ReflectUtils.getMethodInfo(setter.getWriteMethod());
					if (useConverter) {
						final Type setterType = write.getSignature().getArgumentTypes()[0];
						e.load_local(targetLocal);
						e.load_arg(2);
						e.load_local(sourceLocal);
						e.invoke(read);
						e.box(read.getSignature().getReturnType());
						EmitUtils.load_class(e, setterType);
						e.push(write.getSignature().getName());
						e.invoke_interface(CONVERTER, CONVERT);
						e.unbox_or_zero(setterType);
						e.invoke(write);
					} else if (compatible(getter, setter)) {
						e.dup2();
						e.invoke(read);
						e.invoke(write);
					}
				}
			}
			e.return_value();
			e.end_method();
			ce.end_class();
		}

		private static boolean compatible(final PropertyDescriptor getter,
				final PropertyDescriptor setter) {
			// TODO: allow automatic widening conversions?
			return setter.getPropertyType().isAssignableFrom(getter.getPropertyType());
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

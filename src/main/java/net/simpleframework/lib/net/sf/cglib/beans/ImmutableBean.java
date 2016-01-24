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
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

import net.simpleframework.lib.net.sf.cglib.core.AbstractClassGenerator;
import net.simpleframework.lib.net.sf.cglib.core.ClassEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.EmitUtils;
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
public class ImmutableBean {
	private static final Type ILLEGAL_STATE_EXCEPTION = TypeUtils.parseType("IllegalStateException");
	private static final Signature CSTRUCT_OBJECT = TypeUtils.parseConstructor("Object");
	private static final Class[] OBJECT_CLASSES = { Object.class };
	private static final String FIELD_NAME = "CGLIB$RWBean";

	private ImmutableBean() {
	}

	public static Object create(final Object bean) {
		final Generator gen = new Generator();
		gen.setBean(bean);
		return gen.create();
	}

	public static class Generator extends AbstractClassGenerator {
		private static final Source SOURCE = new Source(ImmutableBean.class.getName());
		private Object bean;
		private Class target;

		public Generator() {
			super(SOURCE);
		}

		public void setBean(final Object bean) {
			this.bean = bean;
			target = bean.getClass();
		}

		@Override
		protected ClassLoader getDefaultClassLoader() {
			return target.getClassLoader();
		}

		@Override
		protected ProtectionDomain getProtectionDomain() {
			return ReflectUtils.getProtectionDomain(target);
		}

		public Object create() {
			final String name = target.getName();
			setNamePrefix(name);
			return super.create(name);
		}

		@Override
		public void generateClass(final ClassVisitor v) {
			final Type targetType = Type.getType(target);
			final ClassEmitter ce = new ClassEmitter(v);
			ce.begin_class(Opcodes.V1_2, Opcodes.ACC_PUBLIC, getClassName(), targetType, null,
					Constants.SOURCE_FILE);

			ce.declare_field(Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE, FIELD_NAME, targetType, null);

			CodeEmitter e = ce.begin_method(Opcodes.ACC_PUBLIC, CSTRUCT_OBJECT, null);
			e.load_this();
			e.super_invoke_constructor();
			e.load_this();
			e.load_arg(0);
			e.checkcast(targetType);
			e.putfield(FIELD_NAME);
			e.return_value();
			e.end_method();

			final PropertyDescriptor[] descriptors = ReflectUtils.getBeanProperties(target);
			final Method[] getters = ReflectUtils.getPropertyMethods(descriptors, true, false);
			final Method[] setters = ReflectUtils.getPropertyMethods(descriptors, false, true);

			for (int i = 0; i < getters.length; i++) {
				final MethodInfo getter = ReflectUtils.getMethodInfo(getters[i]);
				e = EmitUtils.begin_method(ce, getter, Opcodes.ACC_PUBLIC);
				e.load_this();
				e.getfield(FIELD_NAME);
				e.invoke(getter);
				e.return_value();
				e.end_method();
			}

			for (int i = 0; i < setters.length; i++) {
				final MethodInfo setter = ReflectUtils.getMethodInfo(setters[i]);
				e = EmitUtils.begin_method(ce, setter, Opcodes.ACC_PUBLIC);
				e.throw_exception(ILLEGAL_STATE_EXCEPTION, "Bean is immutable");
				e.end_method();
			}

			ce.end_class();
		}

		@Override
		protected Object firstInstance(final Class type) {
			return ReflectUtils.newInstance(type, OBJECT_CLASSES, new Object[] { bean });
		}

		// TODO: optimize
		@Override
		protected Object nextInstance(final Object instance) {
			return firstInstance(instance.getClass());
		}
	}
}

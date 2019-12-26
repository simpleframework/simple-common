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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.simpleframework.lib.net.sf.cglib.core.Block;
import net.simpleframework.lib.net.sf.cglib.core.ClassEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.EmitUtils;
import net.simpleframework.lib.net.sf.cglib.core.Local;
import net.simpleframework.lib.net.sf.cglib.core.MethodInfo;
import net.simpleframework.lib.net.sf.cglib.core.ReflectUtils;
import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

class BulkBeanEmitter extends ClassEmitter {
	private static final Signature GET_PROPERTY_VALUES = TypeUtils
			.parseSignature("void getPropertyValues(Object, Object[])");
	private static final Signature SET_PROPERTY_VALUES = TypeUtils
			.parseSignature("void setPropertyValues(Object, Object[])");
	private static final Signature CSTRUCT_EXCEPTION = TypeUtils.parseConstructor("Throwable, int");
	private static final Type BULK_BEAN = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.beans.BulkBean");
	private static final Type BULK_BEAN_EXCEPTION = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.beans.BulkBeanException");

	public BulkBeanEmitter(final ClassVisitor v, final String className, final Class target,
			final String[] getterNames, final String[] setterNames, final Class[] types) {
		super(v);

		final Method[] getters = new Method[getterNames.length];
		final Method[] setters = new Method[setterNames.length];
		validate(target, getterNames, setterNames, types, getters, setters);

		begin_class(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, BULK_BEAN, null,
				Constants.SOURCE_FILE);
		EmitUtils.null_constructor(this);
		generateGet(target, getters);
		generateSet(target, setters);
		end_class();
	}

	private void generateGet(final Class target, final Method[] getters) {
		final CodeEmitter e = begin_method(Opcodes.ACC_PUBLIC, GET_PROPERTY_VALUES, null);
		if (getters.length > 0) {
			e.load_arg(0);
			e.checkcast(Type.getType(target));
			final Local bean = e.make_local();
			e.store_local(bean);
			for (int i = 0; i < getters.length; i++) {
				if (getters[i] != null) {
					final MethodInfo getter = ReflectUtils.getMethodInfo(getters[i]);
					e.load_arg(1);
					e.push(i);
					e.load_local(bean);
					e.invoke(getter);
					e.box(getter.getSignature().getReturnType());
					e.aastore();
				}
			}
		}
		e.return_value();
		e.end_method();
	}

	private void generateSet(final Class target, final Method[] setters) {
		// setPropertyValues
		final CodeEmitter e = begin_method(Opcodes.ACC_PUBLIC, SET_PROPERTY_VALUES, null);
		if (setters.length > 0) {
			final Local index = e.make_local(Type.INT_TYPE);
			e.push(0);
			e.store_local(index);
			e.load_arg(0);
			e.checkcast(Type.getType(target));
			e.load_arg(1);
			final Block handler = e.begin_block();
			int lastIndex = 0;
			for (int i = 0; i < setters.length; i++) {
				if (setters[i] != null) {
					final MethodInfo setter = ReflectUtils.getMethodInfo(setters[i]);
					final int diff = i - lastIndex;
					if (diff > 0) {
						e.iinc(index, diff);
						lastIndex = i;
					}
					e.dup2();
					e.aaload(i);
					e.unbox(setter.getSignature().getArgumentTypes()[0]);
					e.invoke(setter);
				}
			}
			handler.end();
			e.return_value();
			e.catch_exception(handler, Constants.TYPE_THROWABLE);
			e.new_instance(BULK_BEAN_EXCEPTION);
			e.dup_x1();
			e.swap();
			e.load_local(index);
			e.invoke_constructor(BULK_BEAN_EXCEPTION, CSTRUCT_EXCEPTION);
			e.athrow();
		} else {
			e.return_value();
		}
		e.end_method();
	}

	private static void validate(final Class target, final String[] getters, final String[] setters,
			final Class[] types, final Method[] getters_out, final Method[] setters_out) {
		int i = -1;
		if (setters.length != types.length || getters.length != types.length) {
			throw new BulkBeanException("accessor array length must be equal type array length", i);
		}
		try {
			for (i = 0; i < types.length; i++) {
				if (getters[i] != null) {
					final Method method = ReflectUtils.findDeclaredMethod(target, getters[i], null);
					if (method.getReturnType() != types[i]) {
						throw new BulkBeanException("Specified type " + types[i]
								+ " does not match declared type " + method.getReturnType(), i);
					}
					if (Modifier.isPrivate(method.getModifiers())) {
						throw new BulkBeanException("Property is private", i);
					}
					getters_out[i] = method;
				}
				if (setters[i] != null) {
					final Method method = ReflectUtils.findDeclaredMethod(target, setters[i],
							new Class[] { types[i] });
					if (Modifier.isPrivate(method.getModifiers())) {
						throw new BulkBeanException("Property is private", i);
					}
					setters_out[i] = method;
				}
			}
		} catch (final NoSuchMethodException e) {
			throw new BulkBeanException("Cannot find specified property", i);
		}
	}
}

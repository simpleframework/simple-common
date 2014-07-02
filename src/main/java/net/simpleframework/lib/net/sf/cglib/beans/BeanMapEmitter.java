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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.simpleframework.lib.net.sf.cglib.core.ClassEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.EmitUtils;
import net.simpleframework.lib.net.sf.cglib.core.MethodInfo;
import net.simpleframework.lib.net.sf.cglib.core.ObjectSwitchCallback;
import net.simpleframework.lib.net.sf.cglib.core.ReflectUtils;
import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.Label;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

class BeanMapEmitter extends ClassEmitter {
	private static final Type BEAN_MAP = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.beans.BeanMap");
	private static final Type FIXED_KEY_SET = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.beans.FixedKeySet");
	private static final Signature CSTRUCT_OBJECT = TypeUtils.parseConstructor("Object");
	private static final Signature CSTRUCT_STRING_ARRAY = TypeUtils.parseConstructor("String[]");
	private static final Signature BEAN_MAP_GET = TypeUtils
			.parseSignature("Object get(Object, Object)");
	private static final Signature BEAN_MAP_PUT = TypeUtils
			.parseSignature("Object put(Object, Object, Object)");
	private static final Signature KEY_SET = TypeUtils.parseSignature("java.util.Set keySet()");
	private static final Signature NEW_INSTANCE = new Signature("newInstance", BEAN_MAP,
			new Type[] { Constants.TYPE_OBJECT });
	private static final Signature GET_PROPERTY_TYPE = TypeUtils
			.parseSignature("Class getPropertyType(String)");

	public BeanMapEmitter(final ClassVisitor v, final String className, final Class type,
			final int require) {
		super(v);

		begin_class(Opcodes.V1_2, Opcodes.ACC_PUBLIC, className, BEAN_MAP, null,
				Constants.SOURCE_FILE);
		EmitUtils.null_constructor(this);
		EmitUtils.factory_method(this, NEW_INSTANCE);
		generateConstructor();

		final Map getters = makePropertyMap(ReflectUtils.getBeanGetters(type));
		final Map setters = makePropertyMap(ReflectUtils.getBeanSetters(type));
		final Map allProps = new HashMap();
		allProps.putAll(getters);
		allProps.putAll(setters);

		if (require != 0) {
			for (final Iterator it = allProps.keySet().iterator(); it.hasNext();) {
				final String name = (String) it.next();
				if ((((require & BeanMap.REQUIRE_GETTER) != 0) && !getters.containsKey(name))
						|| (((require & BeanMap.REQUIRE_SETTER) != 0) && !setters.containsKey(name))) {
					it.remove();
					getters.remove(name);
					setters.remove(name);
				}
			}
		}
		generateGet(type, getters);
		generatePut(type, setters);

		final String[] allNames = getNames(allProps);
		generateKeySet(allNames);
		generateGetPropertyType(allProps, allNames);
		end_class();
	}

	private Map makePropertyMap(final PropertyDescriptor[] props) {
		final Map names = new HashMap();
		for (int i = 0; i < props.length; i++) {
			names.put(props[i].getName(), props[i]);
		}
		return names;
	}

	private String[] getNames(final Map propertyMap) {
		return (String[]) propertyMap.keySet().toArray(new String[propertyMap.size()]);
	}

	private void generateConstructor() {
		final CodeEmitter e = begin_method(Opcodes.ACC_PUBLIC, CSTRUCT_OBJECT, null);
		e.load_this();
		e.load_arg(0);
		e.super_invoke_constructor(CSTRUCT_OBJECT);
		e.return_value();
		e.end_method();
	}

	private void generateGet(final Class type, final Map getters) {
		final CodeEmitter e = begin_method(Opcodes.ACC_PUBLIC, BEAN_MAP_GET, null);
		e.load_arg(0);
		e.checkcast(Type.getType(type));
		e.load_arg(1);
		e.checkcast(Constants.TYPE_STRING);
		EmitUtils.string_switch(e, getNames(getters), Constants.SWITCH_STYLE_HASH,
				new ObjectSwitchCallback() {
					@Override
					public void processCase(final Object key, final Label end) {
						final PropertyDescriptor pd = (PropertyDescriptor) getters.get(key);
						final MethodInfo method = ReflectUtils.getMethodInfo(pd.getReadMethod());
						e.invoke(method);
						e.box(method.getSignature().getReturnType());
						e.return_value();
					}

					@Override
					public void processDefault() {
						e.aconst_null();
						e.return_value();
					}
				});
		e.end_method();
	}

	private void generatePut(final Class type, final Map setters) {
		final CodeEmitter e = begin_method(Opcodes.ACC_PUBLIC, BEAN_MAP_PUT, null);
		e.load_arg(0);
		e.checkcast(Type.getType(type));
		e.load_arg(1);
		e.checkcast(Constants.TYPE_STRING);
		EmitUtils.string_switch(e, getNames(setters), Constants.SWITCH_STYLE_HASH,
				new ObjectSwitchCallback() {
					@Override
					public void processCase(final Object key, final Label end) {
						final PropertyDescriptor pd = (PropertyDescriptor) setters.get(key);
						if (pd.getReadMethod() == null) {
							e.aconst_null();
						} else {
							final MethodInfo read = ReflectUtils.getMethodInfo(pd.getReadMethod());
							e.dup();
							e.invoke(read);
							e.box(read.getSignature().getReturnType());
						}
						e.swap(); // move old value behind bean
						e.load_arg(2); // new value
						final MethodInfo write = ReflectUtils.getMethodInfo(pd.getWriteMethod());
						e.unbox(write.getSignature().getArgumentTypes()[0]);
						e.invoke(write);
						e.return_value();
					}

					@Override
					public void processDefault() {
						// fall-through
					}
				});
		e.aconst_null();
		e.return_value();
		e.end_method();
	}

	private void generateKeySet(final String[] allNames) {
		// static initializer
		declare_field(Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE, "keys", FIXED_KEY_SET, null);

		CodeEmitter e = begin_static();
		e.new_instance(FIXED_KEY_SET);
		e.dup();
		EmitUtils.push_array(e, allNames);
		e.invoke_constructor(FIXED_KEY_SET, CSTRUCT_STRING_ARRAY);
		e.putfield("keys");
		e.return_value();
		e.end_method();

		// keySet
		e = begin_method(Opcodes.ACC_PUBLIC, KEY_SET, null);
		e.load_this();
		e.getfield("keys");
		e.return_value();
		e.end_method();
	}

	private void generateGetPropertyType(final Map allProps, final String[] allNames) {
		final CodeEmitter e = begin_method(Opcodes.ACC_PUBLIC, GET_PROPERTY_TYPE, null);
		e.load_arg(0);
		EmitUtils.string_switch(e, allNames, Constants.SWITCH_STYLE_HASH, new ObjectSwitchCallback() {
			@Override
			public void processCase(final Object key, final Label end) {
				final PropertyDescriptor pd = (PropertyDescriptor) allProps.get(key);
				EmitUtils.load_class(e, Type.getType(pd.getPropertyType()));
				e.return_value();
			}

			@Override
			public void processDefault() {
				e.aconst_null();
				e.return_value();
			}
		});
		e.end_method();
	}
}

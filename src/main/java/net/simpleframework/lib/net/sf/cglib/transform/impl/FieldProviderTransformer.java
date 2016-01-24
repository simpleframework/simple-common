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
package net.simpleframework.lib.net.sf.cglib.transform.impl;

import java.util.HashMap;
import java.util.Map;

import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CodeGenerationException;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.EmitUtils;
import net.simpleframework.lib.net.sf.cglib.core.ObjectSwitchCallback;
import net.simpleframework.lib.net.sf.cglib.core.ProcessSwitchCallback;
import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.net.sf.cglib.transform.ClassEmitterTransformer;
import net.simpleframework.lib.org.objectweb.asm.Label;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

public class FieldProviderTransformer extends ClassEmitterTransformer {

	private static final String FIELD_NAMES = "CGLIB$FIELD_NAMES";
	private static final String FIELD_TYPES = "CGLIB$FIELD_TYPES";

	private static final Type FIELD_PROVIDER = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.transform.impl.FieldProvider");
	private static final Type ILLEGAL_ARGUMENT_EXCEPTION = TypeUtils
			.parseType("IllegalArgumentException");
	private static final Signature PROVIDER_GET = TypeUtils
			.parseSignature("Object getField(String)");
	private static final Signature PROVIDER_SET = TypeUtils
			.parseSignature("void setField(String, Object)");
	private static final Signature PROVIDER_SET_BY_INDEX = TypeUtils
			.parseSignature("void setField(int, Object)");
	private static final Signature PROVIDER_GET_BY_INDEX = TypeUtils
			.parseSignature("Object getField(int)");
	private static final Signature PROVIDER_GET_TYPES = TypeUtils
			.parseSignature("Class[] getFieldTypes()");
	private static final Signature PROVIDER_GET_NAMES = TypeUtils
			.parseSignature("String[] getFieldNames()");

	private int access;
	private Map fields;

	@Override
	public void begin_class(final int version, final int access, final String className,
			final Type superType, Type[] interfaces, final String sourceFile) {
		if (!TypeUtils.isAbstract(access)) {
			interfaces = TypeUtils.add(interfaces, FIELD_PROVIDER);
		}
		this.access = access;
		fields = new HashMap();
		super.begin_class(version, access, className, superType, interfaces, sourceFile);
	}

	@Override
	public void declare_field(final int access, final String name, final Type type,
			final Object value) {
		super.declare_field(access, name, type, value);

		if (!TypeUtils.isStatic(access)) {
			fields.put(name, type);
		}
	}

	@Override
	public void end_class() {
		if (!TypeUtils.isInterface(access)) {
			try {
				generate();
			} catch (final RuntimeException e) {
				throw e;
			} catch (final Exception e) {
				throw new CodeGenerationException(e);
			}
		}
		super.end_class();
	}

	private void generate() throws Exception {
		final String[] names = (String[]) fields.keySet().toArray(new String[fields.size()]);

		final int indexes[] = new int[names.length];
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = i;
		}

		super.declare_field(Constants.PRIVATE_FINAL_STATIC, FIELD_NAMES, Constants.TYPE_STRING_ARRAY,
				null);
		super.declare_field(Constants.PRIVATE_FINAL_STATIC, FIELD_TYPES, Constants.TYPE_CLASS_ARRAY,
				null);

		// use separate methods here because each process switch inner class needs
		// a final CodeEmitter
		initFieldProvider(names);
		getNames();
		getTypes();
		getField(names);
		setField(names);
		setByIndex(names, indexes);
		getByIndex(names, indexes);
	}

	private void initFieldProvider(final String[] names) {
		final CodeEmitter e = getStaticHook();
		EmitUtils.push_object(e, names);
		e.putstatic(getClassType(), FIELD_NAMES, Constants.TYPE_STRING_ARRAY);

		e.push(names.length);
		e.newarray(Constants.TYPE_CLASS);
		e.dup();
		for (int i = 0; i < names.length; i++) {
			e.dup();
			e.push(i);
			final Type type = (Type) fields.get(names[i]);
			EmitUtils.load_class(e, type);
			e.aastore();
		}
		e.putstatic(getClassType(), FIELD_TYPES, Constants.TYPE_CLASS_ARRAY);
	}

	private void getNames() {
		final CodeEmitter e = super.begin_method(Opcodes.ACC_PUBLIC, PROVIDER_GET_NAMES, null);
		e.getstatic(getClassType(), FIELD_NAMES, Constants.TYPE_STRING_ARRAY);
		e.return_value();
		e.end_method();
	}

	private void getTypes() {
		final CodeEmitter e = super.begin_method(Opcodes.ACC_PUBLIC, PROVIDER_GET_TYPES, null);
		e.getstatic(getClassType(), FIELD_TYPES, Constants.TYPE_CLASS_ARRAY);
		e.return_value();
		e.end_method();
	}

	private void setByIndex(final String[] names, final int[] indexes) throws Exception {
		final CodeEmitter e = super.begin_method(Opcodes.ACC_PUBLIC, PROVIDER_SET_BY_INDEX, null);
		e.load_this();
		e.load_arg(1);
		e.load_arg(0);
		e.process_switch(indexes, new ProcessSwitchCallback() {
			@Override
			public void processCase(final int key, final Label end) throws Exception {
				final Type type = (Type) fields.get(names[key]);
				e.unbox(type);
				e.putfield(names[key]);
				e.return_value();
			}

			@Override
			public void processDefault() throws Exception {
				e.throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Unknown field index");
			}
		});
		e.end_method();
	}

	private void getByIndex(final String[] names, final int[] indexes) throws Exception {
		final CodeEmitter e = super.begin_method(Opcodes.ACC_PUBLIC, PROVIDER_GET_BY_INDEX, null);
		e.load_this();
		e.load_arg(0);
		e.process_switch(indexes, new ProcessSwitchCallback() {
			@Override
			public void processCase(final int key, final Label end) throws Exception {
				final Type type = (Type) fields.get(names[key]);
				e.getfield(names[key]);
				e.box(type);
				e.return_value();
			}

			@Override
			public void processDefault() throws Exception {
				e.throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Unknown field index");
			}
		});
		e.end_method();
	}

	// TODO: if this is used to enhance class files SWITCH_STYLE_TRIE should be
	// used
	// to avoid JVM hashcode implementation incompatibilities
	private void getField(final String[] names) throws Exception {
		final CodeEmitter e = begin_method(Opcodes.ACC_PUBLIC, PROVIDER_GET, null);
		e.load_this();
		e.load_arg(0);
		EmitUtils.string_switch(e, names, Constants.SWITCH_STYLE_HASH, new ObjectSwitchCallback() {
			@Override
			public void processCase(final Object key, final Label end) {
				final Type type = (Type) fields.get(key);
				e.getfield((String) key);
				e.box(type);
				e.return_value();
			}

			@Override
			public void processDefault() {
				e.throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Unknown field name");
			}
		});
		e.end_method();
	}

	private void setField(final String[] names) throws Exception {
		final CodeEmitter e = begin_method(Opcodes.ACC_PUBLIC, PROVIDER_SET, null);
		e.load_this();
		e.load_arg(1);
		e.load_arg(0);
		EmitUtils.string_switch(e, names, Constants.SWITCH_STYLE_HASH, new ObjectSwitchCallback() {
			@Override
			public void processCase(final Object key, final Label end) {
				final Type type = (Type) fields.get(key);
				e.unbox(type);
				e.putfield((String) key);
				e.return_value();
			}

			@Override
			public void processDefault() {
				e.throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Unknown field name");
			}
		});
		e.end_method();
	}
}

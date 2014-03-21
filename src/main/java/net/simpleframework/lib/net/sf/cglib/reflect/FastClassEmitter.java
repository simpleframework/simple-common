/*
 * Copyright 2003,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.simpleframework.lib.net.sf.cglib.reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.simpleframework.lib.net.sf.cglib.core.Block;
import net.simpleframework.lib.net.sf.cglib.core.ClassEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CollectionUtils;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.DuplicatesPredicate;
import net.simpleframework.lib.net.sf.cglib.core.EmitUtils;
import net.simpleframework.lib.net.sf.cglib.core.MethodInfo;
import net.simpleframework.lib.net.sf.cglib.core.MethodInfoTransformer;
import net.simpleframework.lib.net.sf.cglib.core.ObjectSwitchCallback;
import net.simpleframework.lib.net.sf.cglib.core.ProcessSwitchCallback;
import net.simpleframework.lib.net.sf.cglib.core.ReflectUtils;
import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.net.sf.cglib.core.Transformer;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.net.sf.cglib.core.VisibilityPredicate;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.Label;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

class FastClassEmitter extends ClassEmitter {
	private static final Signature CSTRUCT_CLASS = TypeUtils.parseConstructor("Class");
	private static final Signature METHOD_GET_INDEX = TypeUtils
			.parseSignature("int getIndex(String, Class[])");
	private static final Signature SIGNATURE_GET_INDEX = new Signature("getIndex", Type.INT_TYPE,
			new Type[] { Constants.TYPE_SIGNATURE });
	private static final Signature TO_STRING = TypeUtils.parseSignature("String toString()");
	private static final Signature CONSTRUCTOR_GET_INDEX = TypeUtils
			.parseSignature("int getIndex(Class[])");
	private static final Signature INVOKE = TypeUtils
			.parseSignature("Object invoke(int, Object, Object[])");
	private static final Signature NEW_INSTANCE = TypeUtils
			.parseSignature("Object newInstance(int, Object[])");
	private static final Signature GET_MAX_INDEX = TypeUtils.parseSignature("int getMaxIndex()");
	private static final Signature GET_SIGNATURE_WITHOUT_RETURN_TYPE = TypeUtils
			.parseSignature("String getSignatureWithoutReturnType(String, Class[])");
	private static final Type FAST_CLASS = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.reflect.FastClass");
	private static final Type ILLEGAL_ARGUMENT_EXCEPTION = TypeUtils
			.parseType("IllegalArgumentException");
	private static final Type INVOCATION_TARGET_EXCEPTION = TypeUtils
			.parseType("java.lang.reflect.InvocationTargetException");
	private static final Type[] INVOCATION_TARGET_EXCEPTION_ARRAY = { INVOCATION_TARGET_EXCEPTION };

	public FastClassEmitter(final ClassVisitor v, final String className, final Class type) {
		super(v);

		final Type base = Type.getType(type);
		begin_class(Opcodes.V1_2, Opcodes.ACC_PUBLIC, className, FAST_CLASS, null,
				Constants.SOURCE_FILE);

		// constructor
		CodeEmitter e = begin_method(Opcodes.ACC_PUBLIC, CSTRUCT_CLASS, null);
		e.load_this();
		e.load_args();
		e.super_invoke_constructor(CSTRUCT_CLASS);
		e.return_value();
		e.end_method();

		final VisibilityPredicate vp = new VisibilityPredicate(type, false);
		final List methods = ReflectUtils.addAllMethods(type, new ArrayList());
		CollectionUtils.filter(methods, vp);
		CollectionUtils.filter(methods, new DuplicatesPredicate());
		final List constructors = new ArrayList(Arrays.asList(type.getDeclaredConstructors()));
		CollectionUtils.filter(constructors, vp);

		// getIndex(String)
		emitIndexBySignature(methods);

		// getIndex(String, Class[])
		emitIndexByClassArray(methods);

		// getIndex(Class[])
		e = begin_method(Opcodes.ACC_PUBLIC, CONSTRUCTOR_GET_INDEX, null);
		e.load_args();
		final List info = CollectionUtils
				.transform(constructors, MethodInfoTransformer.getInstance());
		EmitUtils.constructor_switch(e, info, new GetIndexCallback(e, info));
		e.end_method();

		// invoke(int, Object, Object[])
		e = begin_method(Opcodes.ACC_PUBLIC, INVOKE, INVOCATION_TARGET_EXCEPTION_ARRAY);
		e.load_arg(1);
		e.checkcast(base);
		e.load_arg(0);
		invokeSwitchHelper(e, methods, 2, base);
		e.end_method();

		// newInstance(int, Object[])
		e = begin_method(Opcodes.ACC_PUBLIC, NEW_INSTANCE, INVOCATION_TARGET_EXCEPTION_ARRAY);
		e.new_instance(base);
		e.dup();
		e.load_arg(0);
		invokeSwitchHelper(e, constructors, 1, base);
		e.end_method();

		// getMaxIndex()
		e = begin_method(Opcodes.ACC_PUBLIC, GET_MAX_INDEX, null);
		e.push(methods.size() - 1);
		e.return_value();
		e.end_method();

		end_class();
	}

	// TODO: support constructor indices ("<init>")
	private void emitIndexBySignature(final List methods) {
		final CodeEmitter e = begin_method(Opcodes.ACC_PUBLIC, SIGNATURE_GET_INDEX, null);
		final List signatures = CollectionUtils.transform(methods, new Transformer() {
			@Override
			public Object transform(final Object obj) {
				return ReflectUtils.getSignature((Method) obj).toString();
			}
		});
		e.load_arg(0);
		e.invoke_virtual(Constants.TYPE_OBJECT, TO_STRING);
		signatureSwitchHelper(e, signatures);
		e.end_method();
	}

	private static final int TOO_MANY_METHODS = 100; // TODO

	private void emitIndexByClassArray(final List methods) {
		final CodeEmitter e = begin_method(Opcodes.ACC_PUBLIC, METHOD_GET_INDEX, null);
		if (methods.size() > TOO_MANY_METHODS) {
			// hack for big classes
			final List signatures = CollectionUtils.transform(methods, new Transformer() {
				@Override
				public Object transform(final Object obj) {
					final String s = ReflectUtils.getSignature((Method) obj).toString();
					return s.substring(0, s.lastIndexOf(')') + 1);
				}
			});
			e.load_args();
			e.invoke_static(FAST_CLASS, GET_SIGNATURE_WITHOUT_RETURN_TYPE);
			signatureSwitchHelper(e, signatures);
		} else {
			e.load_args();
			final List info = CollectionUtils.transform(methods, MethodInfoTransformer.getInstance());
			EmitUtils.method_switch(e, info, new GetIndexCallback(e, info));
		}
		e.end_method();
	}

	private void signatureSwitchHelper(final CodeEmitter e, final List signatures) {
		final ObjectSwitchCallback callback = new ObjectSwitchCallback() {
			@Override
			public void processCase(final Object key, final Label end) {
				// TODO: remove linear indexOf
				e.push(signatures.indexOf(key));
				e.return_value();
			}

			@Override
			public void processDefault() {
				e.push(-1);
				e.return_value();
			}
		};
		EmitUtils.string_switch(e, (String[]) signatures.toArray(new String[signatures.size()]),
				Constants.SWITCH_STYLE_HASH, callback);
	}

	private static void invokeSwitchHelper(final CodeEmitter e, final List members, final int arg,
			final Type base) {
		final List info = CollectionUtils.transform(members, MethodInfoTransformer.getInstance());
		final Label illegalArg = e.make_label();
		final Block block = e.begin_block();
		e.process_switch(getIntRange(info.size()), new ProcessSwitchCallback() {
			@Override
			public void processCase(final int key, final Label end) {
				final MethodInfo method = (MethodInfo) info.get(key);
				final Type[] types = method.getSignature().getArgumentTypes();
				for (int i = 0; i < types.length; i++) {
					e.load_arg(arg);
					e.aaload(i);
					e.unbox(types[i]);
				}
				// TODO: change method lookup process so MethodInfo will already
				// reference base
				// instead of superclass when superclass method is inaccessible
				e.invoke(method, base);
				if (!TypeUtils.isConstructor(method)) {
					e.box(method.getSignature().getReturnType());
				}
				e.return_value();
			}

			@Override
			public void processDefault() {
				e.goTo(illegalArg);
			}
		});
		block.end();
		EmitUtils.wrap_throwable(block, INVOCATION_TARGET_EXCEPTION);
		e.mark(illegalArg);
		e.throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Cannot find matching method/constructor");
	}

	private static class GetIndexCallback implements ObjectSwitchCallback {
		private final CodeEmitter e;
		private final Map indexes = new HashMap();

		public GetIndexCallback(final CodeEmitter e, final List methods) {
			this.e = e;
			int index = 0;
			for (final Iterator it = methods.iterator(); it.hasNext();) {
				indexes.put(it.next(), new Integer(index++));
			}
		}

		@Override
		public void processCase(final Object key, final Label end) {
			e.push(((Integer) indexes.get(key)).intValue());
			e.return_value();
		}

		@Override
		public void processDefault() {
			e.push(-1);
			e.return_value();
		}
	}

	private static int[] getIntRange(final int length) {
		final int[] range = new int[length];
		for (int i = 0; i < length; i++) {
			range[i] = i;
		}
		return range;
	}
}

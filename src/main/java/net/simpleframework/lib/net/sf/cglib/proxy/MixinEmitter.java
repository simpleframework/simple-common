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
package net.simpleframework.lib.net.sf.cglib.proxy;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import net.simpleframework.lib.net.sf.cglib.core.ClassEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.EmitUtils;
import net.simpleframework.lib.net.sf.cglib.core.MethodInfo;
import net.simpleframework.lib.net.sf.cglib.core.MethodWrapper;
import net.simpleframework.lib.net.sf.cglib.core.ReflectUtils;
import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

/**
 * @author Chris Nokleberg
 * @version $Id: MixinEmitter.java,v 1.9 2006/08/27 21:04:37 herbyderby Exp $
 */
class MixinEmitter extends ClassEmitter {
	private static final String FIELD_NAME = "CGLIB$DELEGATES";
	private static final Signature CSTRUCT_OBJECT_ARRAY = TypeUtils.parseConstructor("Object[]");
	private static final Type MIXIN = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.proxy.Mixin");
	private static final Signature NEW_INSTANCE = new Signature("newInstance", MIXIN,
			new Type[] { Constants.TYPE_OBJECT_ARRAY });

	public MixinEmitter(final ClassVisitor v, final String className, final Class[] classes,
			final int[] route) {
		super(v);

		begin_class(Opcodes.V1_2, Opcodes.ACC_PUBLIC, className, MIXIN,
				TypeUtils.getTypes(getInterfaces(classes)), Constants.SOURCE_FILE);
		EmitUtils.null_constructor(this);
		EmitUtils.factory_method(this, NEW_INSTANCE);

		declare_field(Opcodes.ACC_PRIVATE, FIELD_NAME, Constants.TYPE_OBJECT_ARRAY, null);

		CodeEmitter e = begin_method(Opcodes.ACC_PUBLIC, CSTRUCT_OBJECT_ARRAY, null);
		e.load_this();
		e.super_invoke_constructor();
		e.load_this();
		e.load_arg(0);
		e.putfield(FIELD_NAME);
		e.return_value();
		e.end_method();

		final Set unique = new HashSet();
		for (int i = 0; i < classes.length; i++) {
			final Method[] methods = getMethods(classes[i]);
			for (int j = 0; j < methods.length; j++) {
				if (unique.add(MethodWrapper.create(methods[j]))) {
					final MethodInfo method = ReflectUtils.getMethodInfo(methods[j]);
					int modifiers = Opcodes.ACC_PUBLIC;
					if ((method.getModifiers() & Opcodes.ACC_VARARGS) == Opcodes.ACC_VARARGS) {
						modifiers |= Opcodes.ACC_VARARGS;
					}
					e = EmitUtils.begin_method(this, method, modifiers);
					e.load_this();
					e.getfield(FIELD_NAME);
					e.aaload((route != null) ? route[i] : i);
					e.checkcast(method.getClassInfo().getType());
					e.load_args();
					e.invoke(method);
					e.return_value();
					e.end_method();
				}
			}
		}

		end_class();
	}

	protected Class[] getInterfaces(final Class[] classes) {
		return classes;
	}

	protected Method[] getMethods(final Class type) {
		return type.getMethods();
	}
}

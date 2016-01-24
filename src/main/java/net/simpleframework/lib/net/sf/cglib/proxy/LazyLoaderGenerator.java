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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.simpleframework.lib.net.sf.cglib.core.ClassEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.MethodInfo;
import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.org.objectweb.asm.Label;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

class LazyLoaderGenerator implements CallbackGenerator {
	public static final LazyLoaderGenerator INSTANCE = new LazyLoaderGenerator();

	private static final Signature LOAD_OBJECT = TypeUtils.parseSignature("Object loadObject()");
	private static final Type LAZY_LOADER = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.proxy.LazyLoader");

	@Override
	public void generate(final ClassEmitter ce, final Context context, final List methods) {
		final Set indexes = new HashSet();
		for (final Iterator it = methods.iterator(); it.hasNext();) {
			final MethodInfo method = (MethodInfo) it.next();
			if (TypeUtils.isProtected(method.getModifiers())) {
				// ignore protected methods
			} else {
				final int index = context.getIndex(method);
				indexes.add(new Integer(index));
				final CodeEmitter e = context.beginMethod(ce, method);
				e.load_this();
				e.dup();
				e.invoke_virtual_this(loadMethod(index));
				e.checkcast(method.getClassInfo().getType());
				e.load_args();
				e.invoke(method);
				e.return_value();
				e.end_method();
			}
		}

		for (final Iterator it = indexes.iterator(); it.hasNext();) {
			final int index = ((Integer) it.next()).intValue();

			final String delegate = "CGLIB$LAZY_LOADER_" + index;
			ce.declare_field(Opcodes.ACC_PRIVATE, delegate, Constants.TYPE_OBJECT, null);

			final CodeEmitter e = ce.begin_method(Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNCHRONIZED
					| Opcodes.ACC_FINAL, loadMethod(index), null);
			e.load_this();
			e.getfield(delegate);
			e.dup();
			final Label end = e.make_label();
			e.ifnonnull(end);
			e.pop();
			e.load_this();
			context.emitCallback(e, index);
			e.invoke_interface(LAZY_LOADER, LOAD_OBJECT);
			e.dup_x1();
			e.putfield(delegate);
			e.mark(end);
			e.return_value();
			e.end_method();

		}
	}

	private Signature loadMethod(final int index) {
		return new Signature("CGLIB$LOAD_PRIVATE_" + index, Constants.TYPE_OBJECT,
				Constants.TYPES_EMPTY);
	}

	@Override
	public void generateStatic(final CodeEmitter e, final Context context, final List methods) {
	}
}

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

import java.util.Iterator;
import java.util.List;

import net.simpleframework.lib.net.sf.cglib.core.Block;
import net.simpleframework.lib.net.sf.cglib.core.ClassEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.EmitUtils;
import net.simpleframework.lib.net.sf.cglib.core.MethodInfo;
import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.org.objectweb.asm.Type;

class InvocationHandlerGenerator implements CallbackGenerator {
	public static final InvocationHandlerGenerator INSTANCE = new InvocationHandlerGenerator();

	private static final Type INVOCATION_HANDLER = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.proxy.InvocationHandler");
	private static final Type UNDECLARED_THROWABLE_EXCEPTION = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.proxy.UndeclaredThrowableException");
	private static final Type METHOD = TypeUtils.parseType("java.lang.reflect.Method");
	private static final Signature INVOKE = TypeUtils
			.parseSignature("Object invoke(Object, java.lang.reflect.Method, Object[])");

	@Override
	public void generate(final ClassEmitter ce, final Context context, final List methods) {
		for (final Iterator it = methods.iterator(); it.hasNext();) {
			final MethodInfo method = (MethodInfo) it.next();
			final Signature impl = context.getImplSignature(method);
			ce.declare_field(Constants.PRIVATE_FINAL_STATIC, impl.getName(), METHOD, null);

			final CodeEmitter e = context.beginMethod(ce, method);
			final Block handler = e.begin_block();
			context.emitCallback(e, context.getIndex(method));
			e.load_this();
			e.getfield(impl.getName());
			e.create_arg_array();
			e.invoke_interface(INVOCATION_HANDLER, INVOKE);
			e.unbox(method.getSignature().getReturnType());
			e.return_value();
			handler.end();
			EmitUtils.wrap_undeclared_throwable(e, handler, method.getExceptionTypes(),
					UNDECLARED_THROWABLE_EXCEPTION);
			e.end_method();
		}
	}

	@Override
	public void generateStatic(final CodeEmitter e, final Context context, final List methods) {
		for (final Iterator it = methods.iterator(); it.hasNext();) {
			final MethodInfo method = (MethodInfo) it.next();
			EmitUtils.load_method(e, method);
			e.putfield(context.getImplSignature(method).getName());
		}
	}
}

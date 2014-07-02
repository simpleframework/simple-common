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

import net.simpleframework.lib.net.sf.cglib.core.ClassEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.MethodInfo;
import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.org.objectweb.asm.Type;

class FixedValueGenerator implements CallbackGenerator {
	public static final FixedValueGenerator INSTANCE = new FixedValueGenerator();
	private static final Type FIXED_VALUE = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.proxy.FixedValue");
	private static final Signature LOAD_OBJECT = TypeUtils.parseSignature("Object loadObject()");

	@Override
	public void generate(final ClassEmitter ce, final Context context, final List methods) {
		for (final Iterator it = methods.iterator(); it.hasNext();) {
			final MethodInfo method = (MethodInfo) it.next();
			final CodeEmitter e = context.beginMethod(ce, method);
			context.emitCallback(e, context.getIndex(method));
			e.invoke_interface(FIXED_VALUE, LOAD_OBJECT);
			e.unbox_or_zero(e.getReturnType());
			e.return_value();
			e.end_method();
		}
	}

	@Override
	public void generateStatic(final CodeEmitter e, final Context context, final List methods) {
	}
}

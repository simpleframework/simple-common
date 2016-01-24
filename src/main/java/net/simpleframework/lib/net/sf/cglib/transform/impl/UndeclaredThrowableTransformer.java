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

import java.lang.reflect.Constructor;

import net.simpleframework.lib.net.sf.cglib.core.Block;
import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.EmitUtils;
import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.net.sf.cglib.transform.ClassEmitterTransformer;
import net.simpleframework.lib.org.objectweb.asm.Type;

public class UndeclaredThrowableTransformer extends ClassEmitterTransformer {
	private final Type wrapper;

	public UndeclaredThrowableTransformer(final Class wrapper) {
		this.wrapper = Type.getType(wrapper);
		boolean found = false;
		final Constructor[] cstructs = wrapper.getConstructors();
		for (int i = 0; i < cstructs.length; i++) {
			final Class[] types = cstructs[i].getParameterTypes();
			if (types.length == 1 && types[0].equals(Throwable.class)) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new IllegalArgumentException(wrapper
					+ " does not have a single-arg constructor that takes a Throwable");
		}
	}

	@Override
	public CodeEmitter begin_method(final int access, final Signature sig, final Type[] exceptions) {
		final CodeEmitter e = super.begin_method(access, sig, exceptions);
		if (TypeUtils.isAbstract(access) || sig.equals(Constants.SIG_STATIC)) {
			return e;
		}
		return new CodeEmitter(e) {
			private Block handler;
			/* init */{
				handler = begin_block();
			}

			@Override
			public void visitMaxs(final int maxStack, final int maxLocals) {
				handler.end();
				EmitUtils.wrap_undeclared_throwable(this, handler, exceptions, wrapper);
				super.visitMaxs(maxStack, maxLocals);
			}
		};
	}
}

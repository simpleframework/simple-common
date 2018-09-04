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
package net.simpleframework.lib.net.sf.cglib.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.org.objectweb.asm.Type;

public class FastMethod extends FastMember {
	FastMethod(final FastClass fc, final Method method) {
		super(fc, method, helper(fc, method));
	}

	private static int helper(final FastClass fc, final Method method) {
		final int index = fc
				.getIndex(new Signature(method.getName(), Type.getMethodDescriptor(method)));
		if (index < 0) {
			final Class[] types = method.getParameterTypes();
			System.err.println("hash=" + method.getName().hashCode() + " size=" + types.length);
			for (int i = 0; i < types.length; i++) {
				System.err.println("  types[" + i + "]=" + types[i].getName());
			}
			throw new IllegalArgumentException("Cannot find method " + method);
		}
		return index;
	}

	public Class getReturnType() {
		return ((Method) member).getReturnType();
	}

	@Override
	public Class[] getParameterTypes() {
		return ((Method) member).getParameterTypes();
	}

	@Override
	public Class[] getExceptionTypes() {
		return ((Method) member).getExceptionTypes();
	}

	public Object invoke(final Object obj, final Object[] args) throws InvocationTargetException {
		return fc.invoke(index, obj, args);
	}

	public Method getJavaMethod() {
		return (Method) member;
	}
}

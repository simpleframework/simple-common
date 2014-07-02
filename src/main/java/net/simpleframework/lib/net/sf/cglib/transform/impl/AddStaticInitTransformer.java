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
package net.simpleframework.lib.net.sf.cglib.transform.impl;

import java.lang.reflect.Method;

import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.EmitUtils;
import net.simpleframework.lib.net.sf.cglib.core.MethodInfo;
import net.simpleframework.lib.net.sf.cglib.core.ReflectUtils;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.net.sf.cglib.transform.ClassEmitterTransformer;
import net.simpleframework.lib.org.objectweb.asm.Type;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class AddStaticInitTransformer extends ClassEmitterTransformer {
	private final MethodInfo info;

	public AddStaticInitTransformer(final Method classInit) {
		info = ReflectUtils.getMethodInfo(classInit);
		if (!TypeUtils.isStatic(info.getModifiers())) {
			throw new IllegalArgumentException(classInit + " is not static");
		}
		final Type[] types = info.getSignature().getArgumentTypes();
		if (types.length != 1 || !types[0].equals(Constants.TYPE_CLASS)
				|| !info.getSignature().getReturnType().equals(Type.VOID_TYPE)) {
			throw new IllegalArgumentException(classInit + " illegal signature");
		}
	}

	@Override
	protected void init() {
		if (!TypeUtils.isInterface(getAccess())) {
			final CodeEmitter e = getStaticHook();
			EmitUtils.load_class_this(e);
			e.invoke(info);
		}
	}
}

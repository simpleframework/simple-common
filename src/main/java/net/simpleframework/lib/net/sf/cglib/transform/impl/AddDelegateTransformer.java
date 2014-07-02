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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CodeGenerationException;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.ReflectUtils;
import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.net.sf.cglib.transform.ClassEmitterTransformer;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

/**
 * @author Juozas Baliuka
 */
public class AddDelegateTransformer extends ClassEmitterTransformer {
	private static final String DELEGATE = "$CGLIB_DELEGATE";
	private static final Signature CSTRUCT_OBJECT = TypeUtils.parseSignature("void <init>(Object)");

	private Class[] delegateIf;
	private Class delegateImpl;
	private Type delegateType;

	/** Creates a new instance of AddDelegateTransformer */
	public AddDelegateTransformer(final Class delegateIf[], final Class delegateImpl) {
		try {
			delegateImpl.getConstructor(new Class[] { Object.class });
			this.delegateIf = delegateIf;
			this.delegateImpl = delegateImpl;
			delegateType = Type.getType(delegateImpl);
		} catch (final NoSuchMethodException e) {
			throw new CodeGenerationException(e);
		}
	}

	@Override
	public void begin_class(final int version, final int access, final String className,
			final Type superType, final Type[] interfaces, final String sourceFile) {

		if (!TypeUtils.isInterface(access)) {

			final Type[] all = TypeUtils.add(interfaces, TypeUtils.getTypes(delegateIf));
			super.begin_class(version, access, className, superType, all, sourceFile);

			declare_field(Opcodes.ACC_PRIVATE | Opcodes.ACC_TRANSIENT, DELEGATE, delegateType, null);
			for (int i = 0; i < delegateIf.length; i++) {
				final Method[] methods = delegateIf[i].getMethods();
				for (int j = 0; j < methods.length; j++) {
					if (Modifier.isAbstract(methods[j].getModifiers())) {
						addDelegate(methods[j]);
					}
				}
			}
		} else {
			super.begin_class(version, access, className, superType, interfaces, sourceFile);
		}
	}

	@Override
	public CodeEmitter begin_method(final int access, final Signature sig, final Type[] exceptions) {
		final CodeEmitter e = super.begin_method(access, sig, exceptions);
		if (sig.getName().equals(Constants.CONSTRUCTOR_NAME)) {
			return new CodeEmitter(e) {
				private boolean transformInit = true;

				@Override
				public void visitMethodInsn(final int opcode, final String owner, final String name,
						final String desc) {
					super.visitMethodInsn(opcode, owner, name, desc);
					if (transformInit && opcode == Opcodes.INVOKESPECIAL) {
						load_this();
						new_instance(delegateType);
						dup();
						load_this();
						invoke_constructor(delegateType, CSTRUCT_OBJECT);
						putfield(DELEGATE);
						transformInit = false;
					}
				}
			};
		}
		return e;
	}

	private void addDelegate(final Method m) {
		Method delegate;
		try {
			delegate = delegateImpl.getMethod(m.getName(), m.getParameterTypes());
			if (!delegate.getReturnType().getName().equals(m.getReturnType().getName())) {
				throw new IllegalArgumentException("Invalid delegate signature " + delegate);
			}
		} catch (final NoSuchMethodException e) {
			throw new CodeGenerationException(e);
		}

		final Signature sig = ReflectUtils.getSignature(m);
		final Type[] exceptions = TypeUtils.getTypes(m.getExceptionTypes());
		final CodeEmitter e = super.begin_method(Opcodes.ACC_PUBLIC, sig, exceptions);
		e.load_this();
		e.getfield(DELEGATE);
		e.load_args();
		e.invoke_virtual(delegateType, sig);
		e.return_value();
		e.end_method();
	}
}

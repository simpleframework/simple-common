/*
 * Copyright 2003 The Apache Software Foundation
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
package net.simpleframework.lib.net.sf.cglib.core;

import java.util.HashMap;
import java.util.Map;

import net.simpleframework.lib.org.objectweb.asm.ClassAdapter;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.FieldVisitor;
import net.simpleframework.lib.org.objectweb.asm.MethodAdapter;
import net.simpleframework.lib.org.objectweb.asm.MethodVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class ClassEmitter extends ClassAdapter {
	private ClassInfo classInfo;
	private Map fieldInfo;

	private static int hookCounter;
	private MethodVisitor rawStaticInit;
	private CodeEmitter staticInit;
	private CodeEmitter staticHook;
	private Signature staticHookSig;

	public ClassEmitter(final ClassVisitor cv) {
		super(null);
		setTarget(cv);
	}

	public ClassEmitter() {
		super(null);
	}

	public void setTarget(final ClassVisitor cv) {
		this.cv = cv;
		fieldInfo = new HashMap();

		// just to be safe
		staticInit = staticHook = null;
		staticHookSig = null;
	}

	synchronized private static int getNextHook() {
		return ++hookCounter;
	}

	public ClassInfo getClassInfo() {
		return classInfo;
	}

	public void begin_class(final int version, final int access, final String className,
			final Type superType, final Type[] interfaces, final String source) {
		final Type classType = Type.getType("L" + className.replace('.', '/') + ";");
		classInfo = new ClassInfo() {
			@Override
			public Type getType() {
				return classType;
			}

			@Override
			public Type getSuperType() {
				return (superType != null) ? superType : Constants.TYPE_OBJECT;
			}

			@Override
			public Type[] getInterfaces() {
				return interfaces;
			}

			@Override
			public int getModifiers() {
				return access;
			}
		};
		cv.visit(version, access, classInfo.getType().getInternalName(), null, classInfo
				.getSuperType().getInternalName(), TypeUtils.toInternalNames(interfaces));
		if (source != null) {
			cv.visitSource(source, null);
		}
		init();
	}

	public CodeEmitter getStaticHook() {
		if (TypeUtils.isInterface(getAccess())) {
			throw new IllegalStateException("static hook is invalid for this class");
		}
		if (staticHook == null) {
			staticHookSig = new Signature("CGLIB$STATICHOOK" + getNextHook(), "()V");
			staticHook = begin_method(Opcodes.ACC_STATIC, staticHookSig, null);
			if (staticInit != null) {
				staticInit.invoke_static_this(staticHookSig);
			}
		}
		return staticHook;
	}

	protected void init() {
	}

	public int getAccess() {
		return classInfo.getModifiers();
	}

	public Type getClassType() {
		return classInfo.getType();
	}

	public Type getSuperType() {
		return classInfo.getSuperType();
	}

	public void end_class() {
		if (staticHook != null && staticInit == null) {
			// force creation of static init
			begin_static();
		}
		if (staticInit != null) {
			staticHook.return_value();
			staticHook.end_method();
			rawStaticInit.visitInsn(Opcodes.RETURN);
			rawStaticInit.visitMaxs(0, 0);
			staticInit = staticHook = null;
			staticHookSig = null;
		}
		cv.visitEnd();
	}

	public CodeEmitter begin_method(final int access, final Signature sig, final Type[] exceptions) {
		if (classInfo == null) {
			throw new IllegalStateException("classInfo is null! " + this);
		}
		final MethodVisitor v = cv.visitMethod(access, sig.getName(), sig.getDescriptor(), null,
				TypeUtils.toInternalNames(exceptions));
		if (sig.equals(Constants.SIG_STATIC) && !TypeUtils.isInterface(getAccess())) {
			rawStaticInit = v;
			final MethodVisitor wrapped = new MethodAdapter(v) {
				@Override
				public void visitMaxs(final int maxStack, final int maxLocals) {
					// ignore
				}

				@Override
				public void visitInsn(final int insn) {
					if (insn != Opcodes.RETURN) {
						super.visitInsn(insn);
					}
				}
			};
			staticInit = new CodeEmitter(this, wrapped, access, sig, exceptions);
			if (staticHook == null) {
				// force static hook creation
				getStaticHook();
			} else {
				staticInit.invoke_static_this(staticHookSig);
			}
			return staticInit;
		} else if (sig.equals(staticHookSig)) {
			return new CodeEmitter(this, v, access, sig, exceptions) {
				@Override
				public boolean isStaticHook() {
					return true;
				}
			};
		} else {
			return new CodeEmitter(this, v, access, sig, exceptions);
		}
	}

	public CodeEmitter begin_static() {
		return begin_method(Opcodes.ACC_STATIC, Constants.SIG_STATIC, null);
	}

	public void declare_field(final int access, final String name, final Type type,
			final Object value) {
		final FieldInfo existing = (FieldInfo) fieldInfo.get(name);
		final FieldInfo info = new FieldInfo(access, name, type, value);
		if (existing != null) {
			if (!info.equals(existing)) {
				throw new IllegalArgumentException("Field \"" + name
						+ "\" has been declared differently");
			}
		} else {
			fieldInfo.put(name, info);
			cv.visitField(access, name, type.getDescriptor(), null, value);
		}
	}

	// TODO: make public?
	boolean isFieldDeclared(final String name) {
		return fieldInfo.get(name) != null;
	}

	FieldInfo getFieldInfo(final String name) {
		final FieldInfo field = (FieldInfo) fieldInfo.get(name);
		if (field == null) {
			throw new IllegalArgumentException("Field " + name + " is not declared in "
					+ getClassType().getClassName());
		}
		return field;
	}

	static class FieldInfo {
		int access;
		String name;
		Type type;
		Object value;

		public FieldInfo(final int access, final String name, final Type type, final Object value) {
			this.access = access;
			this.name = name;
			this.type = type;
			this.value = value;
		}

		@Override
		public boolean equals(final Object o) {
			if (o == null) {
				return false;
			}
			if (!(o instanceof FieldInfo)) {
				return false;
			}
			final FieldInfo other = (FieldInfo) o;
			if (access != other.access || !name.equals(other.name) || !type.equals(other.type)) {
				return false;
			}
			if ((value == null) ^ (other.value == null)) {
				return false;
			}
			if (value != null && !value.equals(other.value)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			return access ^ name.hashCode() ^ type.hashCode()
					^ ((value == null) ? 0 : value.hashCode());
		}
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName, final String[] interfaces) {
		begin_class(version, access, name.replace('/', '.'), TypeUtils.fromInternalName(superName),
				TypeUtils.fromInternalNames(interfaces), null); // TODO
	}

	@Override
	public void visitEnd() {
		end_class();
	}

	@Override
	public FieldVisitor visitField(final int access, final String name, final String desc,
			final String signature, final Object value) {
		declare_field(access, name, Type.getType(desc), value);
		return null; // TODO
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc,
			final String signature, final String[] exceptions) {
		return begin_method(access, new Signature(name, desc),
				TypeUtils.fromInternalNames(exceptions));
	}
}
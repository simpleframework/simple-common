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
package net.simpleframework.lib.net.sf.cglib.core;

import java.util.Arrays;

import net.simpleframework.lib.org.objectweb.asm.Attribute;
import net.simpleframework.lib.org.objectweb.asm.Label;
import net.simpleframework.lib.org.objectweb.asm.MethodVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class CodeEmitter extends LocalVariablesSorter {
	private static final Signature BOOLEAN_VALUE = TypeUtils
			.parseSignature("boolean booleanValue()");
	private static final Signature CHAR_VALUE = TypeUtils.parseSignature("char charValue()");
	private static final Signature LONG_VALUE = TypeUtils.parseSignature("long longValue()");
	private static final Signature DOUBLE_VALUE = TypeUtils.parseSignature("double doubleValue()");
	private static final Signature FLOAT_VALUE = TypeUtils.parseSignature("float floatValue()");
	private static final Signature INT_VALUE = TypeUtils.parseSignature("int intValue()");
	private static final Signature CSTRUCT_NULL = TypeUtils.parseConstructor("");
	private static final Signature CSTRUCT_STRING = TypeUtils.parseConstructor("String");

	public static final int ADD = Constants.IADD;
	public static final int MUL = Constants.IMUL;
	public static final int XOR = Constants.IXOR;
	public static final int USHR = Constants.IUSHR;
	public static final int SUB = Constants.ISUB;
	public static final int DIV = Constants.IDIV;
	public static final int NEG = Constants.INEG;
	public static final int REM = Constants.IREM;
	public static final int AND = Constants.IAND;
	public static final int OR = Constants.IOR;

	public static final int GT = Constants.IFGT;
	public static final int LT = Constants.IFLT;
	public static final int GE = Constants.IFGE;
	public static final int LE = Constants.IFLE;
	public static final int NE = Constants.IFNE;
	public static final int EQ = Constants.IFEQ;

	private final ClassEmitter ce;
	private final State state;

	private static class State extends MethodInfo {
		ClassInfo classInfo;
		int access;
		Signature sig;
		Type[] argumentTypes;
		int localOffset;
		Type[] exceptionTypes;

		State(final ClassInfo classInfo, final int access, final Signature sig,
				final Type[] exceptionTypes) {
			this.classInfo = classInfo;
			this.access = access;
			this.sig = sig;
			this.exceptionTypes = exceptionTypes;
			localOffset = TypeUtils.isStatic(access) ? 0 : 1;
			argumentTypes = sig.getArgumentTypes();
		}

		@Override
		public ClassInfo getClassInfo() {
			return classInfo;
		}

		@Override
		public int getModifiers() {
			return access;
		}

		@Override
		public Signature getSignature() {
			return sig;
		}

		@Override
		public Type[] getExceptionTypes() {
			return exceptionTypes;
		}

		public Attribute getAttribute() {
			// TODO
			return null;
		}
	}

	CodeEmitter(final ClassEmitter ce, final MethodVisitor mv, final int access,
			final Signature sig, final Type[] exceptionTypes) {
		super(access, sig.getDescriptor(), mv);
		this.ce = ce;
		state = new State(ce.getClassInfo(), access, sig, exceptionTypes);
	}

	public CodeEmitter(final CodeEmitter wrap) {
		super(wrap);
		this.ce = wrap.ce;
		this.state = wrap.state;
	}

	public boolean isStaticHook() {
		return false;
	}

	public Signature getSignature() {
		return state.sig;
	}

	public Type getReturnType() {
		return state.sig.getReturnType();
	}

	public MethodInfo getMethodInfo() {
		return state;
	}

	public ClassEmitter getClassEmitter() {
		return ce;
	}

	public void end_method() {
		visitMaxs(0, 0);
	}

	public Block begin_block() {
		return new Block(this);
	}

	public void catch_exception(final Block block, final Type exception) {
		if (block.getEnd() == null) {
			throw new IllegalStateException("end of block is unset");
		}
		mv.visitTryCatchBlock(block.getStart(), block.getEnd(), mark(), exception.getInternalName());
	}

	public void goTo(final Label label) {
		mv.visitJumpInsn(Opcodes.GOTO, label);
	}

	public void ifnull(final Label label) {
		mv.visitJumpInsn(Opcodes.IFNULL, label);
	}

	public void ifnonnull(final Label label) {
		mv.visitJumpInsn(Opcodes.IFNONNULL, label);
	}

	public void if_jump(final int mode, final Label label) {
		mv.visitJumpInsn(mode, label);
	}

	public void if_icmp(final int mode, final Label label) {
		if_cmp(Type.INT_TYPE, mode, label);
	}

	public void if_cmp(final Type type, final int mode, final Label label) {
		int intOp = -1;
		int jumpmode = mode;
		switch (mode) {
		case GE:
			jumpmode = LT;
			break;
		case LE:
			jumpmode = GT;
			break;
		}
		switch (type.getSort()) {
		case Type.LONG:
			mv.visitInsn(Opcodes.LCMP);
			break;
		case Type.DOUBLE:
			mv.visitInsn(Opcodes.DCMPG);
			break;
		case Type.FLOAT:
			mv.visitInsn(Opcodes.FCMPG);
			break;
		case Type.ARRAY:
		case Type.OBJECT:
			switch (mode) {
			case EQ:
				mv.visitJumpInsn(Opcodes.IF_ACMPEQ, label);
				return;
			case NE:
				mv.visitJumpInsn(Opcodes.IF_ACMPNE, label);
				return;
			}
			throw new IllegalArgumentException("Bad comparison for type " + type);
		default:
			switch (mode) {
			case EQ:
				intOp = Opcodes.IF_ICMPEQ;
				break;
			case NE:
				intOp = Opcodes.IF_ICMPNE;
				break;
			case GE:
				swap(); /* fall through */
			case LT:
				intOp = Opcodes.IF_ICMPLT;
				break;
			case LE:
				swap(); /* fall through */
			case GT:
				intOp = Opcodes.IF_ICMPGT;
				break;
			}
			mv.visitJumpInsn(intOp, label);
			return;
		}
		if_jump(jumpmode, label);
	}

	public void pop() {
		mv.visitInsn(Opcodes.POP);
	}

	public void pop2() {
		mv.visitInsn(Opcodes.POP2);
	}

	public void dup() {
		mv.visitInsn(Opcodes.DUP);
	}

	public void dup2() {
		mv.visitInsn(Opcodes.DUP2);
	}

	public void dup_x1() {
		mv.visitInsn(Opcodes.DUP_X1);
	}

	public void dup_x2() {
		mv.visitInsn(Opcodes.DUP_X2);
	}

	public void dup2_x1() {
		mv.visitInsn(Opcodes.DUP2_X1);
	}

	public void dup2_x2() {
		mv.visitInsn(Opcodes.DUP2_X2);
	}

	public void swap() {
		mv.visitInsn(Opcodes.SWAP);
	}

	public void aconst_null() {
		mv.visitInsn(Opcodes.ACONST_NULL);
	}

	public void swap(final Type prev, final Type type) {
		if (type.getSize() == 1) {
			if (prev.getSize() == 1) {
				swap(); // same as dup_x1(), pop();
			} else {
				dup_x2();
				pop();
			}
		} else {
			if (prev.getSize() == 1) {
				dup2_x1();
				pop2();
			} else {
				dup2_x2();
				pop2();
			}
		}
	}

	public void monitorenter() {
		mv.visitInsn(Opcodes.MONITORENTER);
	}

	public void monitorexit() {
		mv.visitInsn(Opcodes.MONITOREXIT);
	}

	public void math(final int op, final Type type) {
		mv.visitInsn(type.getOpcode(op));
	}

	public void array_load(final Type type) {
		mv.visitInsn(type.getOpcode(Opcodes.IALOAD));
	}

	public void array_store(final Type type) {
		mv.visitInsn(type.getOpcode(Opcodes.IASTORE));
	}

	/**
	 * Casts from one primitive numeric type to another
	 */
	public void cast_numeric(final Type from, final Type to) {
		if (from != to) {
			if (from == Type.DOUBLE_TYPE) {
				if (to == Type.FLOAT_TYPE) {
					mv.visitInsn(Opcodes.D2F);
				} else if (to == Type.LONG_TYPE) {
					mv.visitInsn(Opcodes.D2L);
				} else {
					mv.visitInsn(Opcodes.D2I);
					cast_numeric(Type.INT_TYPE, to);
				}
			} else if (from == Type.FLOAT_TYPE) {
				if (to == Type.DOUBLE_TYPE) {
					mv.visitInsn(Opcodes.F2D);
				} else if (to == Type.LONG_TYPE) {
					mv.visitInsn(Opcodes.F2L);
				} else {
					mv.visitInsn(Opcodes.F2I);
					cast_numeric(Type.INT_TYPE, to);
				}
			} else if (from == Type.LONG_TYPE) {
				if (to == Type.DOUBLE_TYPE) {
					mv.visitInsn(Opcodes.L2D);
				} else if (to == Type.FLOAT_TYPE) {
					mv.visitInsn(Opcodes.L2F);
				} else {
					mv.visitInsn(Opcodes.L2I);
					cast_numeric(Type.INT_TYPE, to);
				}
			} else {
				if (to == Type.BYTE_TYPE) {
					mv.visitInsn(Opcodes.I2B);
				} else if (to == Type.CHAR_TYPE) {
					mv.visitInsn(Opcodes.I2C);
				} else if (to == Type.DOUBLE_TYPE) {
					mv.visitInsn(Opcodes.I2D);
				} else if (to == Type.FLOAT_TYPE) {
					mv.visitInsn(Opcodes.I2F);
				} else if (to == Type.LONG_TYPE) {
					mv.visitInsn(Opcodes.I2L);
				} else if (to == Type.SHORT_TYPE) {
					mv.visitInsn(Opcodes.I2S);
				}
			}
		}
	}

	public void push(final int i) {
		if (i < -1) {
			mv.visitLdcInsn(new Integer(i));
		} else if (i <= 5) {
			mv.visitInsn(TypeUtils.ICONST(i));
		} else if (i <= Byte.MAX_VALUE) {
			mv.visitIntInsn(Opcodes.BIPUSH, i);
		} else if (i <= Short.MAX_VALUE) {
			mv.visitIntInsn(Opcodes.SIPUSH, i);
		} else {
			mv.visitLdcInsn(new Integer(i));
		}
	}

	public void push(final long value) {
		if (value == 0L || value == 1L) {
			mv.visitInsn(TypeUtils.LCONST(value));
		} else {
			mv.visitLdcInsn(new Long(value));
		}
	}

	public void push(final float value) {
		if (value == 0f || value == 1f || value == 2f) {
			mv.visitInsn(TypeUtils.FCONST(value));
		} else {
			mv.visitLdcInsn(new Float(value));
		}
	}

	public void push(final double value) {
		if (value == 0d || value == 1d) {
			mv.visitInsn(TypeUtils.DCONST(value));
		} else {
			mv.visitLdcInsn(new Double(value));
		}
	}

	public void push(final String value) {
		mv.visitLdcInsn(value);
	}

	public void newarray() {
		newarray(Constants.TYPE_OBJECT);
	}

	public void newarray(final Type type) {
		if (TypeUtils.isPrimitive(type)) {
			mv.visitIntInsn(Opcodes.NEWARRAY, TypeUtils.NEWARRAY(type));
		} else {
			emit_type(Opcodes.ANEWARRAY, type);
		}
	}

	public void arraylength() {
		mv.visitInsn(Opcodes.ARRAYLENGTH);
	}

	public void load_this() {
		if (TypeUtils.isStatic(state.access)) {
			throw new IllegalStateException("no 'this' pointer within static method");
		}
		mv.visitVarInsn(Opcodes.ALOAD, 0);
	}

	/**
	 * Pushes all of the arguments of the current method onto the stack.
	 */
	public void load_args() {
		load_args(0, state.argumentTypes.length);
	}

	/**
	 * Pushes the specified argument of the current method onto the stack.
	 * 
	 * @param index
	 *           the zero-based index into the argument list
	 */
	public void load_arg(final int index) {
		load_local(state.argumentTypes[index], state.localOffset + skipArgs(index));
	}

	// zero-based (see load_this)
	public void load_args(final int fromArg, final int count) {
		int pos = state.localOffset + skipArgs(fromArg);
		for (int i = 0; i < count; i++) {
			final Type t = state.argumentTypes[fromArg + i];
			load_local(t, pos);
			pos += t.getSize();
		}
	}

	private int skipArgs(final int numArgs) {
		int amount = 0;
		for (int i = 0; i < numArgs; i++) {
			amount += state.argumentTypes[i].getSize();
		}
		return amount;
	}

	private void load_local(final Type t, final int pos) {
		// TODO: make t == null ok?
		mv.visitVarInsn(t.getOpcode(Opcodes.ILOAD), pos);
	}

	private void store_local(final Type t, final int pos) {
		// TODO: make t == null ok?
		mv.visitVarInsn(t.getOpcode(Opcodes.ISTORE), pos);
	}

	public void iinc(final Local local, final int amount) {
		mv.visitIincInsn(local.getIndex(), amount);
	}

	public void store_local(final Local local) {
		store_local(local.getType(), local.getIndex());
	}

	public void load_local(final Local local) {
		load_local(local.getType(), local.getIndex());
	}

	public void return_value() {
		mv.visitInsn(state.sig.getReturnType().getOpcode(Opcodes.IRETURN));
	}

	public void getfield(final String name) {
		final ClassEmitter.FieldInfo info = ce.getFieldInfo(name);
		final int opcode = TypeUtils.isStatic(info.access) ? Opcodes.GETSTATIC : Opcodes.GETFIELD;
		emit_field(opcode, ce.getClassType(), name, info.type);
	}

	public void putfield(final String name) {
		final ClassEmitter.FieldInfo info = ce.getFieldInfo(name);
		final int opcode = TypeUtils.isStatic(info.access) ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD;
		emit_field(opcode, ce.getClassType(), name, info.type);
	}

	public void super_getfield(final String name, final Type type) {
		emit_field(Opcodes.GETFIELD, ce.getSuperType(), name, type);
	}

	public void super_putfield(final String name, final Type type) {
		emit_field(Opcodes.PUTFIELD, ce.getSuperType(), name, type);
	}

	public void super_getstatic(final String name, final Type type) {
		emit_field(Opcodes.GETSTATIC, ce.getSuperType(), name, type);
	}

	public void super_putstatic(final String name, final Type type) {
		emit_field(Opcodes.PUTSTATIC, ce.getSuperType(), name, type);
	}

	public void getfield(final Type owner, final String name, final Type type) {
		emit_field(Opcodes.GETFIELD, owner, name, type);
	}

	public void putfield(final Type owner, final String name, final Type type) {
		emit_field(Opcodes.PUTFIELD, owner, name, type);
	}

	public void getstatic(final Type owner, final String name, final Type type) {
		emit_field(Opcodes.GETSTATIC, owner, name, type);
	}

	public void putstatic(final Type owner, final String name, final Type type) {
		emit_field(Opcodes.PUTSTATIC, owner, name, type);
	}

	// package-protected for EmitUtils, try to fix
	void emit_field(final int opcode, final Type ctype, final String name, final Type ftype) {
		mv.visitFieldInsn(opcode, ctype.getInternalName(), name, ftype.getDescriptor());
	}

	public void super_invoke() {
		super_invoke(state.sig);
	}

	public void super_invoke(final Signature sig) {
		emit_invoke(Opcodes.INVOKESPECIAL, ce.getSuperType(), sig);
	}

	public void invoke_constructor(final Type type) {
		invoke_constructor(type, CSTRUCT_NULL);
	}

	public void super_invoke_constructor() {
		invoke_constructor(ce.getSuperType());
	}

	public void invoke_constructor_this() {
		invoke_constructor(ce.getClassType());
	}

	private void emit_invoke(final int opcode, final Type type, final Signature sig) {
		if (sig.getName().equals(Constants.CONSTRUCTOR_NAME)
				&& ((opcode == Opcodes.INVOKEVIRTUAL) || (opcode == Opcodes.INVOKESTATIC))) {
			// TODO: error
		}
		mv.visitMethodInsn(opcode, type.getInternalName(), sig.getName(), sig.getDescriptor());
	}

	public void invoke_interface(final Type owner, final Signature sig) {
		emit_invoke(Opcodes.INVOKEINTERFACE, owner, sig);
	}

	public void invoke_virtual(final Type owner, final Signature sig) {
		emit_invoke(Opcodes.INVOKEVIRTUAL, owner, sig);
	}

	public void invoke_static(final Type owner, final Signature sig) {
		emit_invoke(Opcodes.INVOKESTATIC, owner, sig);
	}

	public void invoke_virtual_this(final Signature sig) {
		invoke_virtual(ce.getClassType(), sig);
	}

	public void invoke_static_this(final Signature sig) {
		invoke_static(ce.getClassType(), sig);
	}

	public void invoke_constructor(final Type type, final Signature sig) {
		emit_invoke(Opcodes.INVOKESPECIAL, type, sig);
	}

	public void invoke_constructor_this(final Signature sig) {
		invoke_constructor(ce.getClassType(), sig);
	}

	public void super_invoke_constructor(final Signature sig) {
		invoke_constructor(ce.getSuperType(), sig);
	}

	public void new_instance_this() {
		new_instance(ce.getClassType());
	}

	public void new_instance(final Type type) {
		emit_type(Opcodes.NEW, type);
	}

	private void emit_type(final int opcode, final Type type) {
		String desc;
		if (TypeUtils.isArray(type)) {
			desc = type.getDescriptor();
		} else {
			desc = type.getInternalName();
		}
		mv.visitTypeInsn(opcode, desc);
	}

	public void aaload(final int index) {
		push(index);
		aaload();
	}

	public void aaload() {
		mv.visitInsn(Opcodes.AALOAD);
	}

	public void aastore() {
		mv.visitInsn(Opcodes.AASTORE);
	}

	public void athrow() {
		mv.visitInsn(Opcodes.ATHROW);
	}

	public Label make_label() {
		return new Label();
	}

	public Local make_local() {
		return make_local(Constants.TYPE_OBJECT);
	}

	public Local make_local(final Type type) {
		return new Local(newLocal(type.getSize()), type);
	}

	public void checkcast_this() {
		checkcast(ce.getClassType());
	}

	public void checkcast(final Type type) {
		if (!type.equals(Constants.TYPE_OBJECT)) {
			emit_type(Opcodes.CHECKCAST, type);
		}
	}

	public void instance_of(final Type type) {
		emit_type(Opcodes.INSTANCEOF, type);
	}

	public void instance_of_this() {
		instance_of(ce.getClassType());
	}

	public void process_switch(final int[] keys, final ProcessSwitchCallback callback) {
		float density;
		if (keys.length == 0) {
			density = 0;
		} else {
			density = (float) keys.length / (keys[keys.length - 1] - keys[0] + 1);
		}
		process_switch(keys, callback, density >= 0.5f);
	}

	public void process_switch(final int[] keys, final ProcessSwitchCallback callback,
			final boolean useTable) {
		if (!isSorted(keys)) {
			throw new IllegalArgumentException("keys to switch must be sorted ascending");
		}
		final Label def = make_label();
		final Label end = make_label();

		try {
			if (keys.length > 0) {
				final int len = keys.length;
				final int min = keys[0];
				final int max = keys[len - 1];
				final int range = max - min + 1;

				if (useTable) {
					final Label[] labels = new Label[range];
					Arrays.fill(labels, def);
					for (int i = 0; i < len; i++) {
						labels[keys[i] - min] = make_label();
					}
					mv.visitTableSwitchInsn(min, max, def, labels);
					for (int i = 0; i < range; i++) {
						final Label label = labels[i];
						if (label != def) {
							mark(label);
							callback.processCase(i + min, end);
						}
					}
				} else {
					final Label[] labels = new Label[len];
					for (int i = 0; i < len; i++) {
						labels[i] = make_label();
					}
					mv.visitLookupSwitchInsn(def, keys, labels);
					for (int i = 0; i < len; i++) {
						mark(labels[i]);
						callback.processCase(keys[i], end);
					}
				}
			}

			mark(def);
			callback.processDefault();
			mark(end);

		} catch (final RuntimeException e) {
			throw e;
		} catch (final Error e) {
			throw e;
		} catch (final Exception e) {
			throw new CodeGenerationException(e);
		}
	}

	private static boolean isSorted(final int[] keys) {
		for (int i = 1; i < keys.length; i++) {
			if (keys[i] < keys[i - 1]) {
				return false;
			}
		}
		return true;
	}

	public void mark(final Label label) {
		mv.visitLabel(label);
	}

	Label mark() {
		final Label label = make_label();
		mv.visitLabel(label);
		return label;
	}

	public void push(final boolean value) {
		push(value ? 1 : 0);
	}

	/**
	 * Toggles the integer on the top of the stack from 1 to 0 or vice versa
	 */
	public void not() {
		push(1);
		math(XOR, Type.INT_TYPE);
	}

	public void throw_exception(final Type type, final String msg) {
		new_instance(type);
		dup();
		push(msg);
		invoke_constructor(type, CSTRUCT_STRING);
		athrow();
	}

	/**
	 * If the argument is a primitive class, replaces the primitive value on the
	 * top of the stack with the wrapped (Object) equivalent. For example, char
	 * -> Character. If the class is Void, a null is pushed onto the stack
	 * instead.
	 * 
	 * @param type
	 *           the class indicating the current type of the top stack value
	 */
	public void box(final Type type) {
		if (TypeUtils.isPrimitive(type)) {
			if (type == Type.VOID_TYPE) {
				aconst_null();
			} else {
				final Type boxed = TypeUtils.getBoxedType(type);
				new_instance(boxed);
				if (type.getSize() == 2) {
					// Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
					dup_x2();
					dup_x2();
					pop();
				} else {
					// p -> po -> opo -> oop -> o
					dup_x1();
					swap();
				}
				invoke_constructor(boxed, new Signature(Constants.CONSTRUCTOR_NAME, Type.VOID_TYPE,
						new Type[] { type }));
			}
		}
	}

	/**
	 * If the argument is a primitive class, replaces the object on the top of
	 * the stack with the unwrapped (primitive) equivalent. For example,
	 * Character -> char.
	 * 
	 * @param type
	 *           the class indicating the desired type of the top stack value
	 * @return true if the value was unboxed
	 */
	public void unbox(final Type type) {
		Type t = Constants.TYPE_NUMBER;
		Signature sig = null;
		switch (type.getSort()) {
		case Type.VOID:
			return;
		case Type.CHAR:
			t = Constants.TYPE_CHARACTER;
			sig = CHAR_VALUE;
			break;
		case Type.BOOLEAN:
			t = Constants.TYPE_BOOLEAN;
			sig = BOOLEAN_VALUE;
			break;
		case Type.DOUBLE:
			sig = DOUBLE_VALUE;
			break;
		case Type.FLOAT:
			sig = FLOAT_VALUE;
			break;
		case Type.LONG:
			sig = LONG_VALUE;
			break;
		case Type.INT:
		case Type.SHORT:
		case Type.BYTE:
			sig = INT_VALUE;
		}

		if (sig == null) {
			checkcast(type);
		} else {
			checkcast(t);
			invoke_virtual(t, sig);
		}
	}

	/**
	 * Allocates and fills an Object[] array with the arguments to the current
	 * method. Primitive values are inserted as their boxed (Object) equivalents.
	 */
	public void create_arg_array() {
		/*
		 * generates: Object[] args = new Object[]{ arg1, new Integer(arg2) };
		 */

		push(state.argumentTypes.length);
		newarray();
		for (int i = 0; i < state.argumentTypes.length; i++) {
			dup();
			push(i);
			load_arg(i);
			box(state.argumentTypes[i]);
			aastore();
		}
	}

	/**
	 * Pushes a zero onto the stack if the argument is a primitive class, or a
	 * null otherwise.
	 */
	public void zero_or_null(final Type type) {
		if (TypeUtils.isPrimitive(type)) {
			switch (type.getSort()) {
			case Type.DOUBLE:
				push(0d);
				break;
			case Type.LONG:
				push(0L);
				break;
			case Type.FLOAT:
				push(0f);
				break;
			case Type.VOID:
				aconst_null();
			default:
				push(0);
			}
		} else {
			aconst_null();
		}
	}

	/**
	 * Unboxes the object on the top of the stack. If the object is null, the
	 * unboxed primitive value becomes zero.
	 */
	public void unbox_or_zero(final Type type) {
		if (TypeUtils.isPrimitive(type)) {
			if (type != Type.VOID_TYPE) {
				final Label nonNull = make_label();
				final Label end = make_label();
				dup();
				ifnonnull(nonNull);
				pop();
				zero_or_null(type);
				goTo(end);
				mark(nonNull);
				unbox(type);
				mark(end);
			}
		} else {
			checkcast(type);
		}
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
		if (!TypeUtils.isAbstract(state.access)) {
			mv.visitMaxs(0, 0);
		}
	}

	public void invoke(final MethodInfo method, final Type virtualType) {
		final ClassInfo classInfo = method.getClassInfo();
		final Type type = classInfo.getType();
		final Signature sig = method.getSignature();
		if (sig.getName().equals(Constants.CONSTRUCTOR_NAME)) {
			invoke_constructor(type, sig);
		} else if (TypeUtils.isInterface(classInfo.getModifiers())) {
			invoke_interface(type, sig);
		} else if (TypeUtils.isStatic(method.getModifiers())) {
			invoke_static(type, sig);
		} else {
			invoke_virtual(virtualType, sig);
		}
	}

	public void invoke(final MethodInfo method) {
		invoke(method, method.getClassInfo().getType());
	}
}

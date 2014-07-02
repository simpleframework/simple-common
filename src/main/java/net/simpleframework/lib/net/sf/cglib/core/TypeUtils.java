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
package net.simpleframework.lib.net.sf.cglib.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

public class TypeUtils {
	private static final Map transforms = new HashMap();
	private static final Map rtransforms = new HashMap();

	private TypeUtils() {
	}

	static {
		transforms.put("void", "V");
		transforms.put("byte", "B");
		transforms.put("char", "C");
		transforms.put("double", "D");
		transforms.put("float", "F");
		transforms.put("int", "I");
		transforms.put("long", "J");
		transforms.put("short", "S");
		transforms.put("boolean", "Z");

		CollectionUtils.reverse(transforms, rtransforms);
	}

	public static Type getType(final String className) {
		return Type.getType("L" + className.replace('.', '/') + ";");
	}

	public static boolean isFinal(final int access) {
		return (Opcodes.ACC_FINAL & access) != 0;
	}

	public static boolean isStatic(final int access) {
		return (Opcodes.ACC_STATIC & access) != 0;
	}

	public static boolean isProtected(final int access) {
		return (Opcodes.ACC_PROTECTED & access) != 0;
	}

	public static boolean isPublic(final int access) {
		return (Opcodes.ACC_PUBLIC & access) != 0;
	}

	public static boolean isAbstract(final int access) {
		return (Opcodes.ACC_ABSTRACT & access) != 0;
	}

	public static boolean isInterface(final int access) {
		return (Opcodes.ACC_INTERFACE & access) != 0;
	}

	public static boolean isPrivate(final int access) {
		return (Opcodes.ACC_PRIVATE & access) != 0;
	}

	public static boolean isSynthetic(final int access) {
		return (Opcodes.ACC_SYNTHETIC & access) != 0;
	}

	public static boolean isBridge(final int access) {
		return (Opcodes.ACC_BRIDGE & access) != 0;
	}

	// getPackage returns null on JDK 1.2
	public static String getPackageName(final Type type) {
		return getPackageName(getClassName(type));
	}

	public static String getPackageName(final String className) {
		final int idx = className.lastIndexOf('.');
		return (idx < 0) ? "" : className.substring(0, idx);
	}

	public static String upperFirst(final String s) {
		if (s == null || s.length() == 0) {
			return s;
		}
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	public static String getClassName(final Type type) {
		if (isPrimitive(type)) {
			return (String) rtransforms.get(type.getDescriptor());
		} else if (isArray(type)) {
			return getClassName(getComponentType(type)) + "[]";
		} else {
			return type.getClassName();
		}
	}

	public static Type[] add(final Type[] types, final Type extra) {
		if (types == null) {
			return new Type[] { extra };
		} else {
			final List list = Arrays.asList(types);
			if (list.contains(extra)) {
				return types;
			}
			final Type[] copy = new Type[types.length + 1];
			System.arraycopy(types, 0, copy, 0, types.length);
			copy[types.length] = extra;
			return copy;
		}
	}

	public static Type[] add(final Type[] t1, final Type[] t2) {
		// TODO: set semantics?
		final Type[] all = new Type[t1.length + t2.length];
		System.arraycopy(t1, 0, all, 0, t1.length);
		System.arraycopy(t2, 0, all, t1.length, t2.length);
		return all;
	}

	public static Type fromInternalName(final String name) {
		// TODO; primitives?
		return Type.getType("L" + name + ";");
	}

	public static Type[] fromInternalNames(final String[] names) {
		if (names == null) {
			return null;
		}
		final Type[] types = new Type[names.length];
		for (int i = 0; i < names.length; i++) {
			types[i] = fromInternalName(names[i]);
		}
		return types;
	}

	public static int getStackSize(final Type[] types) {
		int size = 0;
		for (int i = 0; i < types.length; i++) {
			size += types[i].getSize();
		}
		return size;
	}

	public static String[] toInternalNames(final Type[] types) {
		if (types == null) {
			return null;
		}
		final String[] names = new String[types.length];
		for (int i = 0; i < types.length; i++) {
			names[i] = types[i].getInternalName();
		}
		return names;
	}

	public static Signature parseSignature(final String s) {
		final int space = s.indexOf(' ');
		final int lparen = s.indexOf('(', space);
		final int rparen = s.indexOf(')', lparen);
		final String returnType = s.substring(0, space);
		final String methodName = s.substring(space + 1, lparen);
		final StringBuffer sb = new StringBuffer();
		sb.append('(');
		for (final Iterator it = parseTypes(s, lparen + 1, rparen).iterator(); it.hasNext();) {
			sb.append(it.next());
		}
		sb.append(')');
		sb.append(map(returnType));
		return new Signature(methodName, sb.toString());
	}

	public static Type parseType(final String s) {
		return Type.getType(map(s));
	}

	public static Type[] parseTypes(final String s) {
		final List names = parseTypes(s, 0, s.length());
		final Type[] types = new Type[names.size()];
		for (int i = 0; i < types.length; i++) {
			types[i] = Type.getType((String) names.get(i));
		}
		return types;
	}

	public static Signature parseConstructor(final Type[] types) {
		final StringBuffer sb = new StringBuffer();
		sb.append("(");
		for (int i = 0; i < types.length; i++) {
			sb.append(types[i].getDescriptor());
		}
		sb.append(")");
		sb.append("V");
		return new Signature(Constants.CONSTRUCTOR_NAME, sb.toString());
	}

	public static Signature parseConstructor(final String sig) {
		return parseSignature("void <init>(" + sig + ")"); // TODO
	}

	private static List parseTypes(final String s, int mark, final int end) {
		final List types = new ArrayList(5);
		for (;;) {
			final int next = s.indexOf(',', mark);
			if (next < 0) {
				break;
			}
			types.add(map(s.substring(mark, next).trim()));
			mark = next + 1;
		}
		types.add(map(s.substring(mark, end).trim()));
		return types;
	}

	private static String map(String type) {
		if (type.equals("")) {
			return type;
		}
		final String t = (String) transforms.get(type);
		if (t != null) {
			return t;
		} else if (type.indexOf('.') < 0) {
			return map("java.lang." + type);
		} else {
			final StringBuffer sb = new StringBuffer();
			int index = 0;
			while ((index = type.indexOf("[]", index) + 1) > 0) {
				sb.append('[');
			}
			type = type.substring(0, type.length() - sb.length() * 2);
			sb.append('L').append(type.replace('.', '/')).append(';');
			return sb.toString();
		}
	}

	public static Type getBoxedType(final Type type) {
		switch (type.getSort()) {
		case Type.CHAR:
			return Constants.TYPE_CHARACTER;
		case Type.BOOLEAN:
			return Constants.TYPE_BOOLEAN;
		case Type.DOUBLE:
			return Constants.TYPE_DOUBLE;
		case Type.FLOAT:
			return Constants.TYPE_FLOAT;
		case Type.LONG:
			return Constants.TYPE_LONG;
		case Type.INT:
			return Constants.TYPE_INTEGER;
		case Type.SHORT:
			return Constants.TYPE_SHORT;
		case Type.BYTE:
			return Constants.TYPE_BYTE;
		default:
			return type;
		}
	}

	public static Type getUnboxedType(final Type type) {
		if (Constants.TYPE_INTEGER.equals(type)) {
			return Type.INT_TYPE;
		} else if (Constants.TYPE_BOOLEAN.equals(type)) {
			return Type.BOOLEAN_TYPE;
		} else if (Constants.TYPE_DOUBLE.equals(type)) {
			return Type.DOUBLE_TYPE;
		} else if (Constants.TYPE_LONG.equals(type)) {
			return Type.LONG_TYPE;
		} else if (Constants.TYPE_CHARACTER.equals(type)) {
			return Type.CHAR_TYPE;
		} else if (Constants.TYPE_BYTE.equals(type)) {
			return Type.BYTE_TYPE;
		} else if (Constants.TYPE_FLOAT.equals(type)) {
			return Type.FLOAT_TYPE;
		} else if (Constants.TYPE_SHORT.equals(type)) {
			return Type.SHORT_TYPE;
		} else {
			return type;
		}
	}

	public static boolean isArray(final Type type) {
		return type.getSort() == Type.ARRAY;
	}

	public static Type getComponentType(final Type type) {
		if (!isArray(type)) {
			throw new IllegalArgumentException("Type " + type + " is not an array");
		}
		return Type.getType(type.getDescriptor().substring(1));
	}

	public static boolean isPrimitive(final Type type) {
		switch (type.getSort()) {
		case Type.ARRAY:
		case Type.OBJECT:
			return false;
		default:
			return true;
		}
	}

	public static String emulateClassGetName(final Type type) {
		if (isArray(type)) {
			return type.getDescriptor().replace('/', '.');
		} else {
			return getClassName(type);
		}
	}

	public static boolean isConstructor(final MethodInfo method) {
		return method.getSignature().getName().equals(Constants.CONSTRUCTOR_NAME);
	}

	public static Type[] getTypes(final Class[] classes) {
		if (classes == null) {
			return null;
		}
		final Type[] types = new Type[classes.length];
		for (int i = 0; i < classes.length; i++) {
			types[i] = Type.getType(classes[i]);
		}
		return types;
	}

	public static int ICONST(final int value) {
		switch (value) {
		case -1:
			return Opcodes.ICONST_M1;
		case 0:
			return Opcodes.ICONST_0;
		case 1:
			return Opcodes.ICONST_1;
		case 2:
			return Opcodes.ICONST_2;
		case 3:
			return Opcodes.ICONST_3;
		case 4:
			return Opcodes.ICONST_4;
		case 5:
			return Opcodes.ICONST_5;
		}
		return -1; // error
	}

	public static int LCONST(final long value) {
		if (value == 0L) {
			return Opcodes.LCONST_0;
		} else if (value == 1L) {
			return Opcodes.LCONST_1;
		} else {
			return -1; // error
		}
	}

	public static int FCONST(final float value) {
		if (value == 0f) {
			return Opcodes.FCONST_0;
		} else if (value == 1f) {
			return Opcodes.FCONST_1;
		} else if (value == 2f) {
			return Opcodes.FCONST_2;
		} else {
			return -1; // error
		}
	}

	public static int DCONST(final double value) {
		if (value == 0d) {
			return Opcodes.DCONST_0;
		} else if (value == 1d) {
			return Opcodes.DCONST_1;
		} else {
			return -1; // error
		}
	}

	public static int NEWARRAY(final Type type) {
		switch (type.getSort()) {
		case Type.BYTE:
			return Opcodes.T_BYTE;
		case Type.CHAR:
			return Opcodes.T_CHAR;
		case Type.DOUBLE:
			return Opcodes.T_DOUBLE;
		case Type.FLOAT:
			return Opcodes.T_FLOAT;
		case Type.INT:
			return Opcodes.T_INT;
		case Type.LONG:
			return Opcodes.T_LONG;
		case Type.SHORT:
			return Opcodes.T_SHORT;
		case Type.BOOLEAN:
			return Opcodes.T_BOOLEAN;
		default:
			return -1; // error
		}
	}

	public static String escapeType(final String s) {
		final StringBuffer sb = new StringBuffer();
		for (int i = 0, len = s.length(); i < len; i++) {
			final char c = s.charAt(i);
			switch (c) {
			case '$':
				sb.append("$24");
				break;
			case '.':
				sb.append("$2E");
				break;
			case '[':
				sb.append("$5B");
				break;
			case ';':
				sb.append("$3B");
				break;
			case '(':
				sb.append("$28");
				break;
			case ')':
				sb.append("$29");
				break;
			case '/':
				sb.append("$2F");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}
}

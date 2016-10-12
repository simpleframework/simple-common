/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
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

package net.simpleframework.lib.org.mvel2.ast;

import static java.lang.Character.isDigit;
import static net.simpleframework.lib.org.mvel2.ast.ASTNode.COMPILE_IMMEDIATE;
import static net.simpleframework.lib.org.mvel2.util.ArrayTools.findFirst;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.balancedCapture;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.createClass;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.findClass;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.isWhitespace;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.repeatChar;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subCompileExpression;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subset;
import static net.simpleframework.lib.org.mvel2.util.ReflectionUtil.toPrimitiveArrayType;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.AbstractParser;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.util.ParseTools;

public class TypeDescriptor implements Serializable {
	private String className;
	private char[] expr;

	private int start;
	private int offset;

	private ArraySize[] arraySize;
	private ExecutableStatement[] compiledArraySize;
	int endRange;

	public TypeDescriptor(final char[] name, final int start, final int offset, final int fields) {
		updateClassName(this.expr = name, this.start = start, this.offset = offset, fields);
	}

	public void updateClassName(final char[] name, final int start, final int offset,
			final int fields) {
		this.expr = name;

		if (offset == 0 || !ParseTools.isIdentifierPart(name[start]) || isDigit(name[start])) {
			return;
		}

		if ((endRange = findFirst('(', start, offset, name)) == -1) {
			if ((endRange = findFirst('[', start, offset, name)) != -1) {
				className = new String(name, start, endRange - start).trim();
				int to;

				final LinkedList<char[]> sizes = new LinkedList<char[]>();

				final int end = start + offset;
				while (endRange < end) {
					while (endRange < end && isWhitespace(name[endRange])) {
						endRange++;
					}

					if (endRange == end || name[endRange] == '{') {
						break;
					}

					if (name[endRange] != '[') {
						throw new CompileException("unexpected token in constructor", name, endRange);
					}
					to = balancedCapture(name, endRange, start + offset, '[');
					sizes.add(subset(name, ++endRange, to - endRange));
					endRange = to + 1;
				}

				final Iterator<char[]> iter = sizes.iterator();
				arraySize = new ArraySize[sizes.size()];

				for (int i = 0; i < arraySize.length; i++) {
					arraySize[i] = new ArraySize(iter.next());
				}

				if ((fields & COMPILE_IMMEDIATE) != 0) {
					compiledArraySize = new ExecutableStatement[arraySize.length];
					for (int i = 0; i < compiledArraySize.length; i++) {
						compiledArraySize[i] = (ExecutableStatement) subCompileExpression(
								arraySize[i].value);
					}
				}

				return;
			}

			className = new String(name, start, offset).trim();
		} else {
			className = new String(name, start, endRange - start).trim();
		}
	}

	public boolean isArray() {
		return arraySize != null;
	}

	public int getArrayLength() {
		return arraySize.length;
	}

	public ArraySize[] getArraySize() {
		return arraySize;
	}

	public ExecutableStatement[] getCompiledArraySize() {
		return compiledArraySize;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	public boolean isClass() {
		return className != null && className.length() != 0;
	}

	public int getEndRange() {
		return endRange;
	}

	public void setEndRange(final int endRange) {
		this.endRange = endRange;
	}

	public Class<?> getClassReference() throws ClassNotFoundException {
		return getClassReference(null, this);
	}

	public Class<?> getClassReference(final ParserContext ctx) throws ClassNotFoundException {
		return getClassReference(ctx, this);
	}

	public static Class getClassReference(final Class baseType, final TypeDescriptor tDescr,
			final VariableResolverFactory factory, final ParserContext ctx)
			throws ClassNotFoundException {
		return findClass(factory,
				repeatChar('[', tDescr.arraySize.length) + "L" + baseType.getName() + ";", ctx);
	}

	public static Class getClassReference(final ParserContext ctx, Class cls,
			final TypeDescriptor tDescr) throws ClassNotFoundException {
		if (tDescr.isArray()) {
			cls = cls.isPrimitive() ? toPrimitiveArrayType(cls)
					: findClass(null,
							repeatChar('[', tDescr.arraySize.length) + "L" + cls.getName() + ";", ctx);
		}
		return cls;
	}

	public static Class getClassReference(final ParserContext ctx, final TypeDescriptor tDescr)
			throws ClassNotFoundException {
		Class cls;
		if (ctx != null && ctx.hasImport(tDescr.className)) {
			cls = ctx.getImport(tDescr.className);
			if (tDescr.isArray()) {
				cls = cls.isPrimitive() ? toPrimitiveArrayType(cls)
						: findClass(null,
								repeatChar('[', tDescr.arraySize.length) + "L" + cls.getName() + ";", ctx);
			}
		} else if (ctx == null && hasContextFreeImport(tDescr.className)) {
			cls = getContextFreeImport(tDescr.className);
			if (tDescr.isArray()) {
				cls = cls.isPrimitive() ? toPrimitiveArrayType(cls)
						: findClass(null,
								repeatChar('[', tDescr.arraySize.length) + "L" + cls.getName() + ";", ctx);
			}
		} else {
			cls = createClass(tDescr.getClassName(), ctx);
			if (tDescr.isArray()) {
				cls = cls.isPrimitive() ? toPrimitiveArrayType(cls)
						: findClass(null,
								repeatChar('[', tDescr.arraySize.length) + "L" + cls.getName() + ";", ctx);
			}
		}

		return cls;
	}

	public boolean isUndimensionedArray() {
		if (arraySize != null) {
			for (final ArraySize anArraySize : arraySize) {
				if (anArraySize.value.length == 0) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean hasContextFreeImport(final String name) {
		return AbstractParser.LITERALS.containsKey(name)
				&& AbstractParser.LITERALS.get(name) instanceof Class;
	}

	public static Class getContextFreeImport(final String name) {
		return (Class) AbstractParser.LITERALS.get(name);
	}

	public char[] getExpr() {
		return expr;
	}

	public int getStart() {
		return start;
	}

	public int getOffset() {
		return offset;
	}
}

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

import static java.lang.reflect.Modifier.isStatic;
import static net.simpleframework.lib.org.mvel2.util.ArrayTools.findLast;

import java.lang.reflect.Method;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Christopher Brock
 */
public class StaticImportNode extends ASTNode {
	private Class declaringClass;
	private String methodName;
	private transient Method method;

	public StaticImportNode(final char[] expr, final int start, final int offset,
			final ParserContext pCtx) {
		super(pCtx);
		try {
			this.expr = expr;
			this.start = start;
			this.offset = offset;

			int mark;

			final ClassLoader classLoader = getClassLoader();

			declaringClass = Class.forName(
					new String(expr, start,
							(mark = findLast('.', start, offset, this.expr = expr)) - start),
					true, classLoader);

			methodName = new String(expr, ++mark, offset - (mark - start));

			if (resolveMethod() == null) {
				throw new CompileException("can not find method for static import: "
						+ declaringClass.getName() + "." + methodName, expr, start);
			}
		} catch (final Exception e) {
			throw new CompileException("unable to import class", expr, start, e);
		}
	}

	private Method resolveMethod() {
		for (final Method meth : declaringClass.getMethods()) {
			if (isStatic(meth.getModifiers()) && methodName.equals(meth.getName())) {
				return method = meth;
			}
		}
		return null;
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		factory.createVariable(methodName, method == null ? method = resolveMethod() : method);
		return null;
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return getReducedValueAccelerated(ctx, thisValue, factory);
	}
}

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

import static net.simpleframework.lib.org.mvel2.util.CompilerTools.expectType;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subCompileExpression;
import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.MVEL;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class Invert extends ASTNode {
	private ExecutableStatement stmt;

	public Invert(final char[] expr, final int start, final int offset, final int fields,
			final ParserContext pCtx) {
		super(pCtx);
		this.expr = expr;
		this.start = start;
		this.offset = offset;

		if ((fields & COMPILE_IMMEDIATE) != 0) {
			expectType(pCtx,
					this.stmt = (ExecutableStatement) subCompileExpression(expr, start, offset, pCtx),
					Integer.class, true);
		}
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return ~((Integer) stmt.getValue(ctx, thisValue, factory));
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		final Object o = MVEL.eval(expr, start, offset, ctx, factory);
		if (o instanceof Integer) {
			return ~((Integer) o);
		} else {
			throw new CompileException("was expecting type: Integer; but found type: "
					+ (o == null ? "null" : o.getClass().getName()), expr, start);
		}
	}
}
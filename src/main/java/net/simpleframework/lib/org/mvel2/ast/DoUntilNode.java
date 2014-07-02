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

import java.util.HashMap;

import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.MapVariableResolverFactory;

/**
 * @author Christopher Brock
 */
public class DoUntilNode extends BlockNode {
	protected String item;
	protected ExecutableStatement condition;

	public DoUntilNode(final char[] expr, final int start, final int offset, final int blockStart,
			final int blockOffset, final ParserContext pCtx) {
		super(pCtx);
		this.expr = expr;
		this.start = start;
		this.offset = offset;

		expectType(
				this.condition = (ExecutableStatement) subCompileExpression(expr, start, offset, pCtx),
				Boolean.class, ((fields & COMPILE_IMMEDIATE) != 0));

		if (pCtx != null) {
			pCtx.pushVariableScope();
		}

		this.compiledBlock = (ExecutableStatement) subCompileExpression(expr, blockStart,
				blockOffset, pCtx);

		if (pCtx != null) {
			pCtx.popVariableScope();
		}
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		final VariableResolverFactory lc = new MapVariableResolverFactory(new HashMap(0), factory);

		do {
			compiledBlock.getValue(ctx, thisValue, lc);
		} while (!(Boolean) condition.getValue(ctx, thisValue, lc));

		return null;
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		final VariableResolverFactory lc = new MapVariableResolverFactory(new HashMap(0), factory);

		do {
			compiledBlock.getValue(ctx, thisValue, lc);
		} while (!(Boolean) condition.getValue(ctx, thisValue, lc));

		return null;
	}

}
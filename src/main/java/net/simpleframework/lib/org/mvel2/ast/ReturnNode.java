/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.simpleframework.lib.org.mvel2.ast;

import static net.simpleframework.lib.org.mvel2.MVEL.eval;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subCompileExpression;
import net.simpleframework.lib.org.mvel2.Operator;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.Accessor;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.StackDemarcResolverFactory;

/**
 * @author Christopher Brock
 */
public class ReturnNode extends ASTNode {

	public ReturnNode(final char[] expr, final int start, final int offset, final int fields,
			final ParserContext pCtx) {
		super(pCtx);
		this.expr = expr;
		this.start = start;
		this.offset = offset;

		if ((fields & COMPILE_IMMEDIATE) != 0) {
			setAccessor((Accessor) subCompileExpression(expr, start, offset, pCtx));
		}
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		if (accessor == null) {
			setAccessor((Accessor) subCompileExpression(expr, start, offset));
		}

		factory.setTiltFlag(true);

		return accessor.getValue(ctx, thisValue, new StackDemarcResolverFactory(factory));
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		factory.setTiltFlag(true);
		return eval(expr, start, offset, ctx, new StackDemarcResolverFactory(factory));
	}

	@Override
	public boolean isOperator() {
		return true;
	}

	@Override
	public Integer getOperator() {
		return Operator.RETURN;
	}

	@Override
	public boolean isOperator(final Integer operator) {
		return Operator.RETURN == operator;
	}
}

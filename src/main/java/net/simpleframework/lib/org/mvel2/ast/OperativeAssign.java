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

import static net.simpleframework.lib.org.mvel2.MVEL.eval;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subCompileExpression;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.math.MathProcessor;
import net.simpleframework.lib.org.mvel2.util.ParseTools;

public class OperativeAssign extends ASTNode {
	private final String varName;
	private ExecutableStatement statement;
	private final int operation;
	private int knownInType = -1;

	public OperativeAssign(final String variableName, final char[] expr, final int start,
			final int offset, final int operation, final int fields, final ParserContext pCtx) {
		super(pCtx);
		this.varName = variableName;
		this.operation = operation;
		this.expr = expr;
		this.start = start;
		this.offset = offset;

		if ((fields & COMPILE_IMMEDIATE) != 0) {
			egressType = (statement = (ExecutableStatement) subCompileExpression(expr, start, offset,
					pCtx)).getKnownEgressType();

			if (pCtx.isStrongTyping()) {
				knownInType = ParseTools.__resolveType(egressType);
			}

			if (!pCtx.hasVarOrInput(varName)) {
				pCtx.addInput(varName, egressType);
			}
		}
	}

	@Override
	public Object getReducedValueAccelerated(Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		final VariableResolver resolver = factory.getVariableResolver(varName);
		resolver.setValue(ctx = MathProcessor.doOperations(resolver.getValue(), operation,
				knownInType, statement.getValue(ctx, thisValue, factory)));
		return ctx;
	}

	@Override
	public Object getReducedValue(Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		final VariableResolver resolver = factory.getVariableResolver(varName);
		resolver.setValue(ctx = MathProcessor.doOperations(resolver.getValue(), operation,
				eval(expr, start, offset, ctx, factory)));
		return ctx;
	}
}
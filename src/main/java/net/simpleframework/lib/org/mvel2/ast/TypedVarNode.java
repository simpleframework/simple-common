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
import static net.simpleframework.lib.org.mvel2.util.ParseTools.checkNameSafety;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.createStringTrimmed;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.find;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subCompileExpression;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Christopher Brock
 */
public class TypedVarNode extends ASTNode implements Assignment {
	private String name;

	private ExecutableStatement statement;

	public TypedVarNode(final char[] expr, final int start, final int offset, int fields,
			final Class type, final ParserContext pCtx) {
		super(pCtx);
		this.egressType = type;
		this.fields = fields;

		this.expr = expr;
		this.start = start;
		this.offset = offset;

		int assignStart;
		if ((assignStart = find(this.expr = expr, start, offset, '=')) != -1) {
			checkNameSafety(name = createStringTrimmed(expr, start, assignStart - start));
			this.offset -= (assignStart - start);
			this.start = assignStart + 1;

			if (((fields |= ASSIGN) & COMPILE_IMMEDIATE) != 0) {
				statement = (ExecutableStatement) subCompileExpression(expr, this.start, this.offset,
						pCtx);
			}
		} else {
			checkNameSafety(name = new String(expr, start, offset));
		}

		if ((fields & COMPILE_IMMEDIATE) != 0) {
			final Class x = pCtx.getVarOrInputType(name);
			if (x != null && x != Object.class && !x.isAssignableFrom(egressType)) {
				throw new RuntimeException("statically-typed variable already defined in scope: "
						+ name);
			}
			pCtx.addVariable(name, egressType, false);
		}
	}

	@Override
	public Object getReducedValueAccelerated(Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		if (statement == null) {
			statement = (ExecutableStatement) subCompileExpression(expr, start, offset);
		}
		factory.createVariable(name, ctx = statement.getValue(ctx, thisValue, factory), egressType);
		return ctx;
	}

	@Override
	public Object getReducedValue(Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		factory.createVariable(name, ctx = eval(expr, start, offset, thisValue, factory), egressType);
		return ctx;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getAssignmentVar() {
		return name;
	}

	@Override
	public char[] getExpression() {
		return expr;
	}

	@Override
	public boolean isNewDeclaration() {
		return true;
	}

	@Override
	public void setValueStatement(final ExecutableStatement stmt) {
		this.statement = stmt;
	}
}

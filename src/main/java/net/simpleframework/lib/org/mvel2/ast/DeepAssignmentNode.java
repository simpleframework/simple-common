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

import static net.simpleframework.lib.org.mvel2.MVEL.compileSetExpression;
import static net.simpleframework.lib.org.mvel2.MVEL.eval;
import static net.simpleframework.lib.org.mvel2.PropertyAccessor.set;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.createShortFormOperativeAssignment;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.createStringTrimmed;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.find;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.skipWhitespace;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subArray;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subCompileExpression;
import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.CompiledAccExpression;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Christopher Brock
 */
public class DeepAssignmentNode extends ASTNode implements Assignment {
	private String property;
	// private char[] stmt;

	private CompiledAccExpression acc;
	private ExecutableStatement statement;

	public DeepAssignmentNode(final char[] expr, final int start, final int offset,
			final int fields, final int operation, final String name, final ParserContext pCtx) {
		super(pCtx);
		this.fields |= DEEP_PROPERTY | fields;

		this.expr = expr;
		this.start = start;
		this.offset = offset;
		int mark;

		if (operation != -1) {
			this.egressType = ((statement = (ExecutableStatement) subCompileExpression(
					createShortFormOperativeAssignment(this.property = name, expr, start, offset,
							operation), pCtx))).getKnownEgressType();
		} else if ((mark = find(expr, start, offset, '=')) != -1) {
			property = createStringTrimmed(expr, start, mark - start);

			// this.start = mark + 1;
			this.start = skipWhitespace(expr, mark + 1);

			if (this.start >= start + offset) {
				throw new CompileException("unexpected end of statement", expr, mark + 1);
			}

			this.offset = offset - (this.start - start);

			if ((fields & COMPILE_IMMEDIATE) != 0) {
				statement = (ExecutableStatement) subCompileExpression(expr, this.start, this.offset,
						pCtx);
			}
		} else {
			property = new String(expr);
		}

		if ((fields & COMPILE_IMMEDIATE) != 0) {
			acc = (CompiledAccExpression) compileSetExpression(property.toCharArray(), start, offset,
					pCtx);
		}
	}

	public DeepAssignmentNode(final char[] expr, final int start, final int offset,
			final int fields, final ParserContext pCtx) {
		this(expr, start, offset, fields, -1, null, pCtx);
	}

	@Override
	public Object getReducedValueAccelerated(Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		if (statement == null) {
			statement = (ExecutableStatement) subCompileExpression(expr, this.start, this.offset);
			acc = (CompiledAccExpression) compileSetExpression(property.toCharArray(),
					statement.getKnownEgressType(), pCtx);
		}
		acc.setValue(ctx, thisValue, factory, ctx = statement.getValue(ctx, thisValue, factory));
		return ctx;
	}

	@Override
	public Object getReducedValue(Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		set(ctx, factory, property, ctx = eval(expr, this.start, this.offset, ctx, factory), pCtx);
		return ctx;
	}

	@Override
	public String getAbsoluteName() {
		return property.substring(0, property.indexOf('.'));
	}

	@Override
	public String getAssignmentVar() {
		return property;
	}

	@Override
	public char[] getExpression() {
		return subArray(expr, start, offset);
	}

	@Override
	public boolean isNewDeclaration() {
		return false;
	}

	@Override
	public boolean isAssignment() {
		return true;
	}

	@Override
	public void setValueStatement(final ExecutableStatement stmt) {
		this.statement = stmt;
	}
}

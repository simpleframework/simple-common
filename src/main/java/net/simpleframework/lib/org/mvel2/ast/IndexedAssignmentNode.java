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

import static net.simpleframework.lib.org.mvel2.MVEL.compileSetExpression;
import static net.simpleframework.lib.org.mvel2.util.ArrayTools.findFirst;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.checkNameSafety;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.createShortFormOperativeAssignment;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.createStringTrimmed;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.find;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.skipWhitespace;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subCompileExpression;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subset;
import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.MVEL;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.CompiledAccExpression;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Christopher Brock
 */
public class IndexedAssignmentNode extends ASTNode implements Assignment {
	private String assignmentVar;
	private String name;
	private int register;
	private transient CompiledAccExpression accExpr;

	private char[] indexTarget;
	private char[] index;

	private char[] stmt;
	private ExecutableStatement statement;

	private boolean col = false;

	public IndexedAssignmentNode(final char[] expr, final int start, final int offset,
			final int fields, final int operation, final String name, final int register,
			final ParserContext pCtx) {
		super(pCtx);
		this.expr = expr;
		this.start = start;
		this.offset = offset;

		this.register = register;

		int assignStart;

		if (operation != -1) {
			checkNameSafety(this.name = name);

			this.egressType = (statement = (ExecutableStatement) subCompileExpression(
					stmt = createShortFormOperativeAssignment(name, expr, start, offset, operation),
					pCtx)).getKnownEgressType();
		} else if ((assignStart = find(expr, start, offset, '=')) != -1) {
			this.name = createStringTrimmed(expr, start, assignStart - start);
			this.assignmentVar = name;

			this.start = skipWhitespace(expr, assignStart + 1);

			if (this.start >= start + offset) {
				throw new CompileException("unexpected end of statement", expr, assignStart + 1);
			}

			this.offset = offset - (this.start - start);
			stmt = subset(expr, this.start, this.offset);

			this.egressType = (statement = (ExecutableStatement) subCompileExpression(expr,
					this.start, this.offset, pCtx)).getKnownEgressType();

			if (col = ((endOfName = (short) findFirst('[', 0, this.name.length(),
					indexTarget = this.name.toCharArray())) > 0)) {
				if (((this.fields |= COLLECTION) & COMPILE_IMMEDIATE) != 0) {
					accExpr = (CompiledAccExpression) compileSetExpression(indexTarget, pCtx);
				}

				this.name = this.name.substring(0, endOfName);
				index = subset(indexTarget, endOfName, indexTarget.length - endOfName);
			}

			checkNameSafety(this.name);
		} else {
			checkNameSafety(this.name = new String(expr));
			this.assignmentVar = name;
		}

		if ((fields & COMPILE_IMMEDIATE) != 0) {
			pCtx.addVariable(name, egressType);
		}
	}

	public IndexedAssignmentNode(final char[] expr, final int start, final int offset,
			final int fields, final int register, final ParserContext pCtx) {
		this(expr, start, offset, fields, -1, null, register, pCtx);
	}

	@Override
	public Object getReducedValueAccelerated(Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		if (accExpr == null && indexTarget != null) {
			accExpr = (CompiledAccExpression) compileSetExpression(indexTarget);
		}

		if (col) {
			accExpr.setValue(ctx, thisValue, factory,
					ctx = statement.getValue(ctx, thisValue, factory));
		} else if (statement != null) {
			if (factory.isIndexedFactory()) {
				factory.createIndexedVariable(register, name,
						ctx = statement.getValue(ctx, thisValue, factory));
			} else {
				factory.createVariable(name, ctx = statement.getValue(ctx, thisValue, factory));
			}
		} else {
			if (factory.isIndexedFactory()) {
				factory.createIndexedVariable(register, name, null);
			} else {
				factory.createVariable(name, statement.getValue(ctx, thisValue, factory));
			}
			return Void.class;
		}

		return ctx;
	}

	@Override
	public Object getReducedValue(Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		checkNameSafety(name);

		if (col) {
			MVEL.setProperty(factory.getIndexedVariableResolver(register).getValue(),
					new String(index), ctx = MVEL.eval(stmt, ctx, factory));
		} else {
			factory.createIndexedVariable(register, name, ctx = MVEL.eval(stmt, ctx, factory));
		}

		return ctx;
	}

	@Override
	public String getAssignmentVar() {
		return assignmentVar;
	}

	public String getVarName() {
		return name;
	}

	@Override
	public char[] getExpression() {
		return stmt;
	}

	public int getRegister() {
		return register;
	}

	public void setRegister(final int register) {
		this.register = register;
	}

	@Override
	public boolean isAssignment() {
		return true;
	}

	@Override
	public String getAbsoluteName() {
		return name;
	}

	@Override
	public boolean isNewDeclaration() {
		return false;
	}

	@Override
	public void setValueStatement(final ExecutableStatement stmt) {
		this.statement = stmt;
	}
}
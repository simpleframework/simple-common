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

import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Christopher Brock
 */
public class IndexedDeclTypedVarNode extends ASTNode implements Assignment {
	private final int register;

	public IndexedDeclTypedVarNode(final int register, final int start, final int offset,
			final Class type, final ParserContext pCtx) {
		super(pCtx);
		this.egressType = type;
		this.start = start;
		this.offset = offset;
		this.register = register;
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		factory.createIndexedVariable(register, null, egressType);
		return ctx;
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		factory.createIndexedVariable(register, null, egressType);
		return null;
	}

	@Override
	public String getAssignmentVar() {
		return null;
	}

	@Override
	public char[] getExpression() {
		return new char[0];
	}

	@Override
	public boolean isAssignment() {
		return true;
	}

	@Override
	public boolean isNewDeclaration() {
		return true;
	}

	@Override
	public void setValueStatement(final ExecutableStatement stmt) {
		throw new RuntimeException("illegal operation");
	}
}
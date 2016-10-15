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

import static net.simpleframework.lib.org.mvel2.util.ParseTools.checkNameSafety;

import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Christopher Brock
 */
public class DeclProtoVarNode extends ASTNode implements Assignment {
	private String name;

	public DeclProtoVarNode(final String name, final Proto type, final int fields,
			final ParserContext pCtx) {
		super(pCtx);
		this.egressType = Proto.ProtoInstance.class;
		checkNameSafety(this.name = name);

		if ((fields & COMPILE_IMMEDIATE) != 0) {
			pCtx.addVariable(name, egressType, true);
		}
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		if (!factory.isResolveable(name)) {
			factory.createVariable(name, null, egressType);
		} else {
			throw new RuntimeException("variable defined within scope: " + name);
		}
		return null;
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		if (!factory.isResolveable(name)) {
			factory.createVariable(name, null, egressType);
		} else {
			throw new RuntimeException("variable defined within scope: " + name);
		}

		return null;
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

	@Override
	public String toString() {
		return "var:" + name;
	}
}
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

import static net.simpleframework.lib.org.mvel2.util.ParseTools.containsCheck;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class Contains extends ASTNode {
	private final ASTNode stmt;
	private final ASTNode stmt2;

	public Contains(final ASTNode stmt, final ASTNode stmt2, final ParserContext pCtx) {
		super(pCtx);
		this.stmt = stmt;
		this.stmt2 = stmt2;
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return containsCheck(stmt.getReducedValueAccelerated(ctx, thisValue, factory),
				stmt2.getReducedValueAccelerated(ctx, thisValue, factory));
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		throw new RuntimeException("operation not supported");
	}

	@Override
	public Class getEgressType() {
		return Boolean.class;
	}

	public ASTNode getFirstStatement() {
		return stmt;
	}

	public ASTNode getSecondStatement() {
		return stmt2;
	}
}

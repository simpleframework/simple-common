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
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class Or extends BooleanNode {

	public Or(final ASTNode left, final ASTNode right, final boolean strongTyping,
			final ParserContext pCtx) {
		super(pCtx);
		expectType(pCtx, this.left = left, Boolean.class, strongTyping);
		expectType(pCtx, this.right = right, Boolean.class, strongTyping);
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return (((Boolean) left.getReducedValueAccelerated(ctx, thisValue, factory))
				|| ((Boolean) right.getReducedValueAccelerated(ctx, thisValue, factory)));
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		throw new RuntimeException("improper use of AST element");
	}

	@Override
	public void setRightMost(final ASTNode right) {
		Or n = this;
		while (n.right != null && n.right instanceof Or) {
			n = (Or) n.right;
		}
		n.right = right;
	}

	@Override
	public ASTNode getRightMost() {
		Or n = this;
		while (n.right != null && n.right instanceof Or) {
			n = (Or) n.right;
		}
		return n.right;
	}

	@Override
	public String toString() {
		return "(" + left.toString() + " || " + right.toString() + ")";
	}

	@Override
	public Class getEgressType() {
		return Boolean.class;
	}
}

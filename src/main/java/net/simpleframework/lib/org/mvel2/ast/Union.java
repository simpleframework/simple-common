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

import static net.simpleframework.lib.org.mvel2.compiler.AbstractParser.getCurrentThreadParserContext;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.PropertyAccessor;
import net.simpleframework.lib.org.mvel2.compiler.Accessor;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.optimizers.AccessorOptimizer;
import net.simpleframework.lib.org.mvel2.optimizers.OptimizerFactory;

public class Union extends ASTNode {
	private final ASTNode main;
	private transient Accessor accessor;

	public Union(final char[] expr, final int start, final int offset, final int fields,
			final ASTNode main, final ParserContext pCtx) {
		super(expr, start, offset, fields, pCtx);
		this.main = main;
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		if (accessor != null) {
			return accessor.getValue(main.getReducedValueAccelerated(ctx, thisValue, factory),
					thisValue, factory);
		} else {
			try {
				final AccessorOptimizer o = OptimizerFactory.getThreadAccessorOptimizer();
				accessor = o.optimizeAccessor(getCurrentThreadParserContext(), expr, start, offset,
						main.getReducedValueAccelerated(ctx, thisValue, factory), thisValue, factory,
						false, main.getEgressType());
				return o.getResultOptPass();
			} finally {
				OptimizerFactory.clearThreadAccessorOptimizer();
			}
		}
	}

	public ASTNode getMain() {
		return main;
	}

	@Override
	public Accessor getAccessor() {
		return accessor;
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return PropertyAccessor.get(expr, start, offset,
				main.getReducedValue(ctx, thisValue, factory), factory, thisValue, pCtx);
	}

	public Class getLeftEgressType() {
		return main.getEgressType();
	}

	@Override
	public String toString() {
		return (main != null ? main.toString() : "") + "-[union]->"
				+ (accessor != null ? accessor.toString() : "");
	}
}

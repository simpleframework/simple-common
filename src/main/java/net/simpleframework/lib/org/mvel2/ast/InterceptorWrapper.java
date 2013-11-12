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
import net.simpleframework.lib.org.mvel2.integration.Interceptor;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Christopher Brock
 */
public class InterceptorWrapper extends ASTNode {
	private final Interceptor interceptor;
	private final ASTNode node;

	public InterceptorWrapper(final Interceptor interceptor, final ASTNode node,
			final ParserContext pCtx) {
		super(pCtx);
		this.interceptor = interceptor;
		this.node = node;
	}

	@Override
	public Object getReducedValueAccelerated(Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		interceptor.doBefore(node, factory);
		interceptor.doAfter(ctx = node.getReducedValueAccelerated(ctx, thisValue, factory), node,
				factory);
		return ctx;
	}

	@Override
	public Object getReducedValue(Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		interceptor.doBefore(node, factory);
		interceptor.doAfter(ctx = node.getReducedValue(ctx, thisValue, factory), node, factory);
		return ctx;
	}
}

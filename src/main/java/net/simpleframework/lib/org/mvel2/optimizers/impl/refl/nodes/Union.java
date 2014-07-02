/**
 * MVEL (The MVFLEX Expression Language)
 *
 * Copyright (C) 2007 Christopher Brock, MVFLEX/Valhalla Project and the
 * Codehaus
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
 *
 */
package net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes;

import static net.simpleframework.lib.org.mvel2.compiler.AbstractParser.getCurrentThreadParserContext;
import net.simpleframework.lib.org.mvel2.compiler.Accessor;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.optimizers.AccessorOptimizer;
import net.simpleframework.lib.org.mvel2.optimizers.OptimizerFactory;

/**
 * @author Christopher Brock
 */
public class Union implements Accessor {
	private final Accessor accessor;
	private final char[] nextExpr;
	private final int start;
	private final int offset;
	private Accessor nextAccessor;

	public Union(final Accessor accessor, final char[] nextAccessor, final int start,
			final int offset) {
		this.accessor = accessor;
		this.start = start;
		this.offset = offset;
		this.nextExpr = nextAccessor;
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) {
		if (nextAccessor == null) {
			return get(ctx, elCtx, variableFactory);
		} else {
			return nextAccessor.getValue(get(ctx, elCtx, variableFactory), elCtx, variableFactory);
		}
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		return nextAccessor.setValue(get(ctx, elCtx, variableFactory), elCtx, variableFactory, value);
	}

	private Object get(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) {
		if (nextAccessor == null) {
			final Object o = accessor.getValue(ctx, elCtx, variableFactory);
			final AccessorOptimizer ao = OptimizerFactory.getDefaultAccessorCompiler();
			final Class ingress = accessor.getKnownEgressType();

			nextAccessor = ao.optimizeAccessor(getCurrentThreadParserContext(), nextExpr, start,
					offset, o, elCtx, variableFactory, false, ingress);
			return ao.getResultOptPass();
		} else {
			return accessor.getValue(ctx, elCtx, variableFactory);
		}
	}

	public Class getLeftIngressType() {
		return accessor.getKnownEgressType();
	}

	@Override
	public Class getKnownEgressType() {
		return nextAccessor.getKnownEgressType();
	}
}

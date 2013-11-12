/**
 * MVEL (The MVFLEX Expression Language)
 *
 * Copyright (C) 2007 Christopher Brock, MVFLEX/Valhalla Project and the Codehaus
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
 *
 */
package net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes;

import net.simpleframework.lib.org.mvel2.ast.Function;
import net.simpleframework.lib.org.mvel2.compiler.Accessor;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class DynamicFunctionAccessor extends BaseAccessor {
	// private Function function;
	private final Accessor[] parameters;

	public DynamicFunctionAccessor(final Accessor[] parms) {
		this.parameters = parms;
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) {
		Object[] parms = null;

		final Function function = (Function) ctx;

		if (parameters != null && parameters.length != 0) {
			parms = new Object[parameters.length];
			for (int i = 0; i < parms.length; i++) {
				parms[i] = parameters[i].getValue(ctx, elCtx, variableFactory);
			}
		}

		if (nextNode != null) {
			return nextNode.getValue(function.call(ctx, elCtx, variableFactory, parms), elCtx,
					variableFactory);
		} else {
			return function.call(ctx, elCtx, variableFactory, parms);
		}
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		throw new RuntimeException("can't write to function");
	}

	@Override
	public Class getKnownEgressType() {
		return Object.class;
	}
}
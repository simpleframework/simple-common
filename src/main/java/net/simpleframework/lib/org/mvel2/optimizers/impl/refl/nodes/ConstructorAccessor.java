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

import java.lang.reflect.Constructor;

import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class ConstructorAccessor extends InvokableAccessor {
	private final Constructor constructor;

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) {
		try {
			if (!coercionNeeded) {
				try {
					if (nextNode != null) {
						return nextNode.getValue(
								constructor.newInstance(executeAll(elCtx, variableFactory)), elCtx,
								variableFactory);
					} else {
						return constructor.newInstance(executeAll(elCtx, variableFactory));
					}
				} catch (final IllegalArgumentException e) {
					coercionNeeded = true;
					return getValue(ctx, elCtx, variableFactory);
				}

			} else {
				if (nextNode != null) {
					return nextNode.getValue(constructor.newInstance(executeAndCoerce(parameterTypes,
							elCtx, variableFactory, constructor.isVarArgs())), elCtx, variableFactory);
				} else {
					return constructor.newInstance(executeAndCoerce(parameterTypes, elCtx,
							variableFactory, constructor.isVarArgs()));
				}
			}
		} catch (final Exception e) {
			throw new RuntimeException("cannot construct object", e);
		}
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		return null;
	}

	private Object[] executeAll(final Object ctx, final VariableResolverFactory vars) {
		if (length == 0) {
			return GetterAccessor.EMPTY;
		}

		final Object[] vals = new Object[length];
		for (int i = 0; i < length; i++) {
			vals[i] = parms[i].getValue(ctx, vars);
		}
		return vals;
	}

	public ConstructorAccessor(final Constructor constructor, final ExecutableStatement[] parms) {
		this.constructor = constructor;
		this.length = (this.parameterTypes = constructor.getParameterTypes()).length;
		this.parms = parms;
	}

	@Override
	public Class getKnownEgressType() {
		return constructor.getClass();
	}

	public Constructor getConstructor() {
		return constructor;
	}

	public ExecutableStatement[] getParameters() {
		return parms;
	}
}

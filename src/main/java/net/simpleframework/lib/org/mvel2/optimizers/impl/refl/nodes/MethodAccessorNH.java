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

import static net.simpleframework.lib.org.mvel2.DataConversion.convert;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.getBestCandidate;

import java.lang.reflect.Method;

import net.simpleframework.lib.org.mvel2.compiler.AccessorNode;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.PropertyHandler;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class MethodAccessorNH implements AccessorNode {
	private AccessorNode nextNode;

	private Method method;
	private Class[] parameterTypes;
	private ExecutableStatement[] parms;
	private int length;
	private boolean coercionNeeded = false;

	private PropertyHandler nullHandler;

	@Override
	public Object getValue(final Object ctx, final Object elCtx, final VariableResolverFactory vars) {
		if (!coercionNeeded) {
			try {
				final Object v = method.invoke(ctx, executeAll(elCtx, vars));
				if (v == null) {
					nullHandler.getProperty(method.getName(), ctx, vars);
				}

				if (nextNode != null) {
					return nextNode.getValue(v, elCtx, vars);
				} else {
					return v;
				}
			} catch (final IllegalArgumentException e) {
				if (ctx != null && method.getDeclaringClass() != ctx.getClass()) {
					final Method o = getBestCandidate(parameterTypes, method.getName(), ctx.getClass(),
							ctx.getClass().getMethods(), true);
					if (o != null) {
						return executeOverrideTarget(o, ctx, elCtx, vars);
					}
				}

				coercionNeeded = true;
				return getValue(ctx, elCtx, vars);
			} catch (final Exception e) {
				throw new RuntimeException("cannot invoke method", e);
			}

		} else {
			try {
				if (nextNode != null) {
					return nextNode
							.getValue(method.invoke(ctx, executeAndCoerce(parameterTypes, elCtx, vars)),
									elCtx, vars);
				} else {
					return method.invoke(ctx, executeAndCoerce(parameterTypes, elCtx, vars));
				}
			} catch (final Exception e) {
				throw new RuntimeException("cannot invoke method", e);
			}
		}
	}

	private Object executeOverrideTarget(final Method o, final Object ctx, final Object elCtx,
			final VariableResolverFactory vars) {
		try {
			Object v = o.invoke(ctx, executeAll(elCtx, vars));
			if (v == null) {
				v = nullHandler.getProperty(o.getName(), ctx, vars);
			}

			if (nextNode != null) {
				return nextNode.getValue(v, elCtx, vars);
			} else {
				return v;
			}
		} catch (final Exception e2) {
			throw new RuntimeException("unable to invoke method", e2);
		}
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

	private Object[] executeAndCoerce(final Class[] target, final Object elCtx,
			final VariableResolverFactory vars) {
		final Object[] values = new Object[length];
		for (int i = 0; i < length; i++) {
			// noinspection unchecked
			values[i] = convert(parms[i].getValue(elCtx, vars), target[i]);
		}
		return values;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(final Method method) {
		this.method = method;
		this.length = (this.parameterTypes = this.method.getParameterTypes()).length;
	}

	public ExecutableStatement[] getParms() {
		return parms;
	}

	public void setParms(final ExecutableStatement[] parms) {
		this.parms = parms;
	}

	public MethodAccessorNH() {
	}

	public MethodAccessorNH(final Method method, final ExecutableStatement[] parms,
			final PropertyHandler handler) {
		this.method = method;
		this.length = (this.parameterTypes = this.method.getParameterTypes()).length;

		this.parms = parms;
		this.nullHandler = handler;
	}

	@Override
	public AccessorNode getNextNode() {
		return nextNode;
	}

	@Override
	public AccessorNode setNextNode(final AccessorNode nextNode) {
		return this.nextNode = nextNode;
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		return nextNode.setValue(ctx, elCtx, variableFactory, value);
	}

	@Override
	public Class getKnownEgressType() {
		return method.getReturnType();
	}
}
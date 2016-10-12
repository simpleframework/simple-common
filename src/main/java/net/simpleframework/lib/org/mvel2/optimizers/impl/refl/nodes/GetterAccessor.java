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

import static net.simpleframework.lib.org.mvel2.MVEL.getProperty;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.getBestCandidate;
import static net.simpleframework.lib.org.mvel2.util.ReflectionUtil.getPropertyFromAccessor;

import java.lang.reflect.Method;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.compiler.AccessorNode;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class GetterAccessor implements AccessorNode {
	private AccessorNode nextNode;
	private final Method method;

	public static final Object[] EMPTY = new Object[0];

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory vars) {
		try {
			if (nextNode != null) {
				return nextNode.getValue(method.invoke(ctx, EMPTY), elCtx, vars);
			} else {
				return method.invoke(ctx, EMPTY);
			}
		} catch (final IllegalArgumentException e) {
			if (ctx != null && method.getDeclaringClass() != ctx.getClass()) {
				final Method o = getBestCandidate(EMPTY, method.getName(), ctx.getClass(),
						ctx.getClass().getMethods(), true);
				if (o != null) {
					return executeOverrideTarget(o, ctx, elCtx, vars);
				}
			}

			/**
			 * HACK: Try to access this another way.
			 */
			if (nextNode != null) {
				return nextNode.getValue(getProperty(getPropertyFromAccessor(method.getName()), ctx),
						elCtx, vars);
			} else {
				return getProperty(getPropertyFromAccessor(method.getName()), ctx);
			}
		} catch (final NullPointerException e) {
			if (ctx == null) {
				throw new RuntimeException(
						"unable to invoke method: " + method.getDeclaringClass().getName() + "."
								+ method.getName() + ": " + "target of method is null",
						e);
			} else {
				throw new RuntimeException("cannot invoke getter: " + method.getName() + " (see trace)",
						e);
			}
		} catch (final Exception e) {
			throw new RuntimeException("cannot invoke getter: " + method.getName() + " [declr.class: "
					+ method.getDeclaringClass().getName() + "; act.class: "
					+ (ctx != null ? ctx.getClass().getName() : "null") + "] (see trace)", e);
		}
	}

	public GetterAccessor(final Method method) {
		this.method = method;
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public AccessorNode setNextNode(final AccessorNode nextNode) {
		return this.nextNode = nextNode;
	}

	@Override
	public AccessorNode getNextNode() {
		return nextNode;
	}

	@Override
	public String toString() {
		return method.getDeclaringClass().getName() + "." + method.getName();
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx, final VariableResolverFactory vars,
			final Object value) {
		try {
			if (nextNode != null) {
				return nextNode.setValue(method.invoke(ctx, EMPTY), elCtx, vars, value);
			} else {
				throw new RuntimeException("bad payload");
			}
		} catch (final IllegalArgumentException e) {
			/**
			 * HACK: Try to access this another way.
			 */

			if (nextNode != null) {
				return nextNode.setValue(getProperty(getPropertyFromAccessor(method.getName()), ctx),
						elCtx, vars, value);
			} else {
				return getProperty(getPropertyFromAccessor(method.getName()), ctx);
			}
		} catch (final CompileException e) {
			throw e;
		} catch (final Exception e) {
			throw new RuntimeException(
					"error " + method.getName() + ": " + e.getClass().getName() + ":" + e.getMessage(),
					e);
		}
	}

	@Override
	public Class getKnownEgressType() {
		return method.getReturnType();
	}

	private Object executeOverrideTarget(final Method o, final Object ctx, final Object elCtx,
			final VariableResolverFactory vars) {
		try {
			if (nextNode != null) {
				return nextNode.getValue(o.invoke(ctx, EMPTY), elCtx, vars);
			} else {
				return o.invoke(ctx, EMPTY);
			}
		} catch (final Exception e2) {
			throw new RuntimeException("unable to invoke method", e2);
		}
	}
}

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

import static net.simpleframework.lib.org.mvel2.DataConversion.convert;

import java.lang.reflect.Method;

import net.simpleframework.lib.org.mvel2.compiler.AccessorNode;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

@SuppressWarnings({ "unchecked" })
public class DynamicSetterAccessor implements AccessorNode {
	// private AccessorNode nextNode;

	private final Method method;
	private final Class targetType;

	public static final Object[] EMPTY = new Object[0];

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		try {
			return method.invoke(ctx, convert(value, targetType));
		} catch (final Exception e) {
			throw new RuntimeException("error binding property", e);
		}
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx, final VariableResolverFactory vars) {
		return null;
	}

	public DynamicSetterAccessor(final Method method) {
		this.method = method;
		this.targetType = method.getParameterTypes()[0];
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public AccessorNode setNextNode(final AccessorNode nextNode) {
		return null;
	}

	@Override
	public AccessorNode getNextNode() {
		return null;
	}

	@Override
	public String toString() {
		return method.getDeclaringClass().getName() + "." + method.getName();
	}

	@Override
	public Class getKnownEgressType() {
		return targetType;
	}
}

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

import java.lang.reflect.Field;

import net.simpleframework.lib.org.mvel2.OptimizationFailure;
import net.simpleframework.lib.org.mvel2.compiler.AccessorNode;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class StaticVarAccessor implements AccessorNode {
	private AccessorNode nextNode;
	Field field;

	@Override
	public Object getValue(final Object ctx, final Object elCtx, final VariableResolverFactory vars) {
		try {
			if (nextNode != null) {
				return nextNode.getValue(field.get(null), elCtx, vars);
			} else {
				return field.get(null);
			}
		} catch (final Exception e) {
			throw new OptimizationFailure("unable to access static field", e);
		}
	}

	public StaticVarAccessor(final Field field) {
		this.field = field;
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
		try {
			if (nextNode == null) {
				field.set(null, value);
			} else {
				return nextNode.setValue(field.get(null), elCtx, variableFactory, value);
			}
		} catch (final Exception e) {
			throw new RuntimeException("error accessing static variable", e);
		}
		return value;
	}

	public Field getField() {
		return field;
	}

	@Override
	public Class getKnownEgressType() {
		return field.getClass();
	}
}
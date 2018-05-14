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

import java.lang.reflect.Field;

import net.simpleframework.lib.org.mvel2.compiler.AccessorNode;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.util.PropertyTools;

public class FieldAccessor implements AccessorNode {
	private AccessorNode nextNode;
	private Field field;
	private boolean coercionRequired = false;
	private boolean primitive;

	public FieldAccessor() {
	}

	public FieldAccessor(final Field field) {
		primitive = (this.field = field).getType().isPrimitive();
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory vars) {
		try {
			if (nextNode != null) {
				return nextNode.getValue(field.get(ctx), elCtx, vars);
			} else {
				return field.get(ctx);
			}
		} catch (final Exception e) {
			throw new RuntimeException("unable to access field: " + field.getName(), e);
		}
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, Object value) {
		if (nextNode != null) {
			try {
				return nextNode.setValue(field.get(ctx), elCtx, variableFactory,
						value == null && primitive
								? PropertyTools.getPrimitiveInitialValue(field.getType())
								: value);
			} catch (final Exception e) {
				throw new RuntimeException("unable to access field", e);
			}
		}

		try {

			if (coercionRequired) {
				field.set(ctx, value = convert(ctx, field.getClass()));
				return value;
			} else {
				field.set(ctx, value);
				return value;
			}
		} catch (final IllegalArgumentException e) {
			if (!coercionRequired) {
				coercionRequired = true;
				return setValue(ctx, elCtx, variableFactory, value);
			}
			throw new RuntimeException("unable to bind property", e);
		} catch (final Exception e) {
			throw new RuntimeException("unable to access field", e);
		}
	}

	public Field getField() {
		return field;
	}

	public void setField(final Field field) {
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
	public Class getKnownEgressType() {
		return field.getClass();
	}
}

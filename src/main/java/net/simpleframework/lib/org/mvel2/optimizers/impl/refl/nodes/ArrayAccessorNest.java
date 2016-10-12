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

import java.lang.reflect.Array;

import net.simpleframework.lib.org.mvel2.compiler.AccessorNode;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.util.ParseTools;

public class ArrayAccessorNest implements AccessorNode {
	private AccessorNode nextNode;
	private ExecutableStatement index;

	private Class baseComponentType;
	private boolean requireConversion;

	public ArrayAccessorNest() {
	}

	public ArrayAccessorNest(final String index) {
		this.index = (ExecutableStatement) ParseTools.subCompileExpression(index.toCharArray());
	}

	public ArrayAccessorNest(final ExecutableStatement stmt) {
		this.index = stmt;
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory vars) {
		if (nextNode != null) {
			return nextNode.getValue(((Object[]) ctx)[(Integer) index.getValue(ctx, elCtx, vars)],
					elCtx, vars);
		} else {
			return ((Object[]) ctx)[(Integer) index.getValue(ctx, elCtx, vars)];
		}
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx, final VariableResolverFactory vars,
			final Object value) {
		if (nextNode != null) {
			return nextNode.setValue(((Object[]) ctx)[(Integer) index.getValue(ctx, elCtx, vars)],
					elCtx, vars, value);
		} else {
			if (baseComponentType == null) {
				baseComponentType = ParseTools.getBaseComponentType(ctx.getClass());
				requireConversion = baseComponentType != value.getClass()
						&& !baseComponentType.isAssignableFrom(value.getClass());
			}

			if (requireConversion) {
				final Object o = convert(value, baseComponentType);
				Array.set(ctx, (Integer) index.getValue(ctx, elCtx, vars), o);
				return o;
			} else {
				Array.set(ctx, (Integer) index.getValue(ctx, elCtx, vars), value);
				return value;
			}
		}
	}

	public ExecutableStatement getIndex() {
		return index;
	}

	public void setIndex(final ExecutableStatement index) {
		this.index = index;
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
		return baseComponentType;
	}

	@Override
	public String toString() {
		return "Array Accessor -> [" + index + "]";
	}
}

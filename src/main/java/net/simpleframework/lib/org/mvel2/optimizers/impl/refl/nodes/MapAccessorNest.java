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

import static net.simpleframework.lib.org.mvel2.util.ParseTools.subCompileExpression;

import java.util.Map;

import net.simpleframework.lib.org.mvel2.DataConversion;
import net.simpleframework.lib.org.mvel2.compiler.AccessorNode;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Christopher Brock
 */
public class MapAccessorNest implements AccessorNode {
	private AccessorNode nextNode;
	private ExecutableStatement property;
	private Class conversionType;

	public MapAccessorNest() {
	}

	public MapAccessorNest(final ExecutableStatement property, final Class conversionType) {
		this.property = property;
		this.conversionType = conversionType;
	}

	public MapAccessorNest(final String property, final Class conversionType) {
		this.property = (ExecutableStatement) subCompileExpression(property.toCharArray());
		this.conversionType = conversionType;
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx, final VariableResolverFactory vrf) {
		if (nextNode != null) {
			return nextNode.getValue(((Map) ctx).get(property.getValue(ctx, elCtx, vrf)), elCtx, vrf);
		} else {
			return ((Map) ctx).get(property.getValue(ctx, elCtx, vrf));
		}
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx, final VariableResolverFactory vars,
			Object value) {
		if (nextNode != null) {
			return nextNode.setValue(((Map) ctx).get(property.getValue(ctx, elCtx, vars)), elCtx,
					vars, value);
		} else {
			if (conversionType != null) {
				((Map) ctx).put(property.getValue(ctx, elCtx, vars),
						value = DataConversion.convert(value, conversionType));
			} else {
				((Map) ctx).put(property.getValue(ctx, elCtx, vars), value);
			}
			return value;
		}
	}

	public ExecutableStatement getProperty() {
		return property;
	}

	public void setProperty(final ExecutableStatement property) {
		this.property = property;
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
	public String toString() {
		return "Map Accessor -> [" + property + "]";
	}

	@Override
	public Class getKnownEgressType() {
		return Object.class;
	}
}

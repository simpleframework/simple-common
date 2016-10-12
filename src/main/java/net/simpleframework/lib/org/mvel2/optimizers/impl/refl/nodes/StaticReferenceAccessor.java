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

import net.simpleframework.lib.org.mvel2.compiler.AccessorNode;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class StaticReferenceAccessor implements AccessorNode {
	private AccessorNode nextNode;

	Object literal;

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory vars) {
		if (nextNode != null) {
			return nextNode.getValue(literal, elCtx, vars);
		} else {
			return literal;
		}
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		return nextNode.setValue(literal, elCtx, variableFactory, value);
	}

	public Object getLiteral() {
		return literal;
	}

	public void setLiteral(final Object literal) {
		this.literal = literal;
	}

	public StaticReferenceAccessor() {
	}

	public StaticReferenceAccessor(final Object literal) {
		this.literal = literal;
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
		return literal.getClass();
	}
}

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

public class IndexedVariableAccessor implements AccessorNode {
	private AccessorNode nextNode;
	private final int register;

	public IndexedVariableAccessor(final int register) {
		this.register = register;
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx, final VariableResolverFactory vrf) {
		if (nextNode != null) {
			return nextNode.getValue(vrf.getIndexedVariableResolver(register).getValue(), elCtx, vrf);
		} else {
			return vrf.getIndexedVariableResolver(register).getValue();
		}
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		if (nextNode != null) {
			return nextNode.setValue(variableFactory.getIndexedVariableResolver(register).getValue(),
					elCtx, variableFactory, value);
		} else {
			variableFactory.getIndexedVariableResolver(register).setValue(value);
			return value;
		}
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
		return Object.class;
	}
}
/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
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
 */

package net.simpleframework.lib.org.mvel2.compiler;

import net.simpleframework.lib.org.mvel2.ast.ASTNode;
import net.simpleframework.lib.org.mvel2.ast.TypeCast;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class ExecutableAccessor implements ExecutableStatement {
	private final ASTNode node;

	private Class ingress;
	private Class egress;
	private boolean convertable;

	public ExecutableAccessor(final ASTNode node, final Class egress) {
		this.node = node;
		this.egress = egress;
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) {
		return node.getReducedValueAccelerated(ctx, elCtx, variableFactory);
	}

	@Override
	public Object getValue(final Object staticContext, final VariableResolverFactory factory) {
		return node.getReducedValueAccelerated(staticContext, staticContext, factory);
	}

	@Override
	public void setKnownIngressType(final Class type) {
		this.ingress = type;
	}

	@Override
	public void setKnownEgressType(final Class type) {
		this.egress = type;
	}

	@Override
	public Class getKnownIngressType() {
		return ingress;
	}

	@Override
	public Class getKnownEgressType() {
		return egress;
	}

	@Override
	public boolean isConvertableIngressEgress() {
		return convertable;
	}

	@Override
	public void computeTypeConversionRule() {
		if (ingress != null && egress != null) {
			convertable = ingress.isAssignableFrom(egress);
		}
	}

	@Override
	public boolean intOptimized() {
		return false;
	}

	public ASTNode getNode() {
		return node;
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		return null;
	}

	@Override
	public boolean isLiteralOnly() {
		return false;
	}

	@Override
	public boolean isExplicitCast() {
		return node instanceof TypeCast;
	}

	@Override
	public boolean isEmptyStatement() {
		return node == null;
	}

	@Override
	public String toString() {
		return node.toString();
	}
}

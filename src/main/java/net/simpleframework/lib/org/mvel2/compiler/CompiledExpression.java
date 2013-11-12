/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
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
 */

package net.simpleframework.lib.org.mvel2.compiler;

import static net.simpleframework.lib.org.mvel2.MVELRuntime.execute;
import static net.simpleframework.lib.org.mvel2.optimizers.OptimizerFactory.setThreadAccessorOptimizer;

import java.io.Serializable;

import net.simpleframework.lib.org.mvel2.ParserConfiguration;
import net.simpleframework.lib.org.mvel2.ast.ASTNode;
import net.simpleframework.lib.org.mvel2.ast.TypeCast;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.ClassImportResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.StackResetResolverFactory;
import net.simpleframework.lib.org.mvel2.optimizers.AccessorOptimizer;
import net.simpleframework.lib.org.mvel2.optimizers.OptimizerFactory;
import net.simpleframework.lib.org.mvel2.util.ASTLinkedList;

public class CompiledExpression implements Serializable, ExecutableStatement {
	private final ASTNode firstNode;

	private Class knownEgressType;
	private Class knownIngressType;

	private boolean convertableIngressEgress;
	private boolean optimized = false;
	private boolean importInjectionRequired = false;
	private final boolean literalOnly;

	private Class<? extends AccessorOptimizer> accessorOptimizer;

	private final String sourceName;

	private final ParserConfiguration parserConfiguration;

	public CompiledExpression(final ASTLinkedList astMap, final String sourceName,
			final Class egressType, final ParserConfiguration parserConfiguration,
			final boolean literalOnly) {
		this.firstNode = astMap.firstNode();
		this.sourceName = sourceName;
		this.knownEgressType = astMap.isSingleNode() ? astMap.firstNonSymbol().getEgressType()
				: egressType;
		this.literalOnly = literalOnly;
		this.parserConfiguration = parserConfiguration;
		this.importInjectionRequired = parserConfiguration.getImports() != null
				&& !parserConfiguration.getImports().isEmpty();
	}

	public ASTNode getFirstNode() {
		return firstNode;
	}

	public boolean isSingleNode() {
		return firstNode != null && firstNode.nextASTNode == null;
	}

	@Override
	public Class getKnownEgressType() {
		return knownEgressType;
	}

	@Override
	public void setKnownEgressType(final Class knownEgressType) {
		this.knownEgressType = knownEgressType;
	}

	@Override
	public Class getKnownIngressType() {
		return knownIngressType;
	}

	@Override
	public void setKnownIngressType(final Class knownIngressType) {
		this.knownIngressType = knownIngressType;
	}

	@Override
	public boolean isConvertableIngressEgress() {
		return convertableIngressEgress;
	}

	@Override
	public void computeTypeConversionRule() {
		if (knownIngressType != null && knownEgressType != null) {
			convertableIngressEgress = knownIngressType.isAssignableFrom(knownEgressType);
		}
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) {
		if (!optimized) {
			setupOptimizers();
			try {
				return getValue(ctx, variableFactory);
			} finally {
				OptimizerFactory.clearThreadAccessorOptimizer();
			}
		}
		return getValue(ctx, variableFactory);
	}

	@Override
	public Object getValue(final Object staticContext, final VariableResolverFactory factory) {
		if (!optimized) {
			setupOptimizers();
			try {
				return getValue(staticContext, factory);
			} finally {
				OptimizerFactory.clearThreadAccessorOptimizer();
			}
		}
		return getDirectValue(staticContext, factory);
	}

	public Object getDirectValue(final Object staticContext, final VariableResolverFactory factory) {
		return execute(false, this, staticContext,
				importInjectionRequired ? new ClassImportResolverFactory(parserConfiguration, factory,
						true) : new StackResetResolverFactory(factory));
	}

	private void setupOptimizers() {
		if (accessorOptimizer != null) {
			setThreadAccessorOptimizer(accessorOptimizer);
		}
		optimized = true;
	}

	public boolean isOptimized() {
		return optimized;
	}

	public Class<? extends AccessorOptimizer> getAccessorOptimizer() {
		return accessorOptimizer;
	}

	public String getSourceName() {
		return sourceName;
	}

	@Override
	public boolean intOptimized() {
		return false;
	}

	public ParserConfiguration getParserConfiguration() {
		return parserConfiguration;
	}

	public boolean isImportInjectionRequired() {
		return importInjectionRequired;
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		return null;
	}

	@Override
	public boolean isLiteralOnly() {
		return literalOnly;
	}

	@Override
	public boolean isEmptyStatement() {
		return firstNode == null;
	}

	@Override
	public boolean isExplicitCast() {
		return firstNode != null && firstNode instanceof TypeCast;
	}

	@Override
	public String toString() {
		final StringBuilder appender = new StringBuilder();
		ASTNode node = firstNode;
		while (node != null) {
			appender.append(node.toString()).append(";\n");
			node = node.nextASTNode;
		}
		return appender.toString();
	}
}

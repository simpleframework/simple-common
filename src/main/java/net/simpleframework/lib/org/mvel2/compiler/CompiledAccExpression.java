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

import static net.simpleframework.lib.org.mvel2.optimizers.OptimizerFactory.getThreadAccessorOptimizer;

import java.io.Serializable;

import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.optimizers.OptimizerFactory;

public class CompiledAccExpression implements ExecutableStatement, Serializable {
	private final char[] expression;
	private final int start;
	private final int offset;

	private transient Accessor accessor;
	private final ParserContext context;
	private Class ingressType;

	public CompiledAccExpression(final char[] expression, final Class ingressType,
			final ParserContext context) {
		this(expression, 0, expression.length, ingressType, context);
	}

	public CompiledAccExpression(final char[] expression, final int start, final int offset,
			final Class ingressType, final ParserContext context) {
		this.expression = expression;
		this.start = start;
		this.offset = offset;

		this.context = context;
		this.ingressType = ingressType != null ? ingressType : Object.class;
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx, final VariableResolverFactory vrf,
			final Object value) {
		if (accessor == null) {
			if (ingressType == Object.class && value != null) {
				ingressType = value.getClass();
			}
			accessor = getThreadAccessorOptimizer().optimizeSetAccessor(context, expression, 0,
					expression.length, ctx, ctx, vrf, false, value, ingressType);

		} else {
			accessor.setValue(ctx, elCtx, vrf, value);
		}
		return value;
	}

	@Override
	public Object getValue(final Object staticContext, final VariableResolverFactory factory) {
		if (accessor == null) {
			try {
				accessor = getThreadAccessorOptimizer().optimizeAccessor(context, expression, 0,
						expression.length, staticContext, staticContext, factory, false, ingressType);
				return getValue(staticContext, factory);
			} finally {
				OptimizerFactory.clearThreadAccessorOptimizer();
			}
		}
		return accessor.getValue(staticContext, staticContext, factory);
	}

	@Override
	public void setKnownIngressType(final Class type) {
		this.ingressType = type;
	}

	@Override
	public void setKnownEgressType(final Class type) {

	}

	@Override
	public Class getKnownIngressType() {
		return ingressType;
	}

	@Override
	public Class getKnownEgressType() {
		return null;
	}

	@Override
	public boolean isConvertableIngressEgress() {
		return false;
	}

	@Override
	public void computeTypeConversionRule() {
	}

	@Override
	public boolean intOptimized() {
		return false;
	}

	@Override
	public boolean isLiteralOnly() {
		return false;
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) {
		if (accessor == null) {
			try {
				accessor = getThreadAccessorOptimizer().optimizeAccessor(context, expression, start,
						offset, ctx, elCtx, variableFactory, false, ingressType);
				return getValue(ctx, elCtx, variableFactory);
			} finally {
				OptimizerFactory.clearThreadAccessorOptimizer();
			}
		}
		return accessor.getValue(ctx, elCtx, variableFactory);
	}

	public Accessor getAccessor() {
		return accessor;
	}

	@Override
	public boolean isEmptyStatement() {
		return accessor == null;
	}

	@Override
	public boolean isExplicitCast() {
		return false;
	}
}

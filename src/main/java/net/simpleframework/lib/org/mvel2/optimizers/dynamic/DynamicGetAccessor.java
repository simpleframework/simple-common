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

package net.simpleframework.lib.org.mvel2.optimizers.dynamic;

import static java.lang.System.currentTimeMillis;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.AbstractParser;
import net.simpleframework.lib.org.mvel2.compiler.Accessor;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.optimizers.AccessorOptimizer;
import net.simpleframework.lib.org.mvel2.optimizers.OptimizerFactory;

public class DynamicGetAccessor implements DynamicAccessor {
	private final char[] expr;
	private final int start;
	private final int offset;

	private long stamp;
	private final int type;

	private int runcount;

	private boolean opt = false;

	private final ParserContext context;

	private final Accessor _safeAccessor;
	private Accessor _accessor;

	public DynamicGetAccessor(final ParserContext context, final char[] expr, final int start,
			final int offset, final int type, final Accessor _accessor) {
		this._safeAccessor = this._accessor = _accessor;
		this.type = type;

		this.expr = expr;
		this.start = start;
		this.offset = offset;

		this.context = context;
		stamp = currentTimeMillis();
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) {
		if (!opt) {
			if (++runcount > DynamicOptimizer.tenuringThreshold) {
				if ((currentTimeMillis() - stamp) < DynamicOptimizer.timeSpan) {
					opt = true;
					return optimize(ctx, elCtx, variableFactory);
				} else {
					runcount = 0;
					stamp = currentTimeMillis();
				}
			}
		}

		return _accessor.getValue(ctx, elCtx, variableFactory);
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		runcount++;
		return _accessor.setValue(ctx, elCtx, variableFactory, value);
	}

	private Object optimize(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableResolverFactory) {

		if (DynamicOptimizer.isOverloaded()) {
			DynamicOptimizer.enforceTenureLimit();
		}

		final AccessorOptimizer ao = OptimizerFactory.getAccessorCompiler("ASM");
		switch (type) {
		case DynamicOptimizer.REGULAR_ACCESSOR:
			_accessor = ao.optimizeAccessor(context, expr, start, offset, ctx, elCtx,
					variableResolverFactory, false, null);
			return ao.getResultOptPass();
		case DynamicOptimizer.OBJ_CREATION:
			_accessor = ao.optimizeObjectCreation(context, expr, start, offset, ctx, elCtx,
					variableResolverFactory);
			return _accessor.getValue(ctx, elCtx, variableResolverFactory);
		case DynamicOptimizer.COLLECTION:
			_accessor = ao.optimizeCollection(AbstractParser.getCurrentThreadParserContext(), ctx,
					null, expr, start, offset, ctx, elCtx, variableResolverFactory);
			return _accessor.getValue(ctx, elCtx, variableResolverFactory);
		}
		return null;
	}

	@Override
	public void deoptimize() {
		this._accessor = this._safeAccessor;
		opt = false;
		runcount = 0;
		stamp = currentTimeMillis();
	}

	public long getStamp() {
		return stamp;
	}

	public int getRuncount() {
		return runcount;
	}

	@Override
	public Class getKnownEgressType() {
		return _safeAccessor.getKnownEgressType();
	}

	public Accessor getAccessor() {
		return _accessor;
	}

	public Accessor getSafeAccessor() {
		return _safeAccessor;
	}
}

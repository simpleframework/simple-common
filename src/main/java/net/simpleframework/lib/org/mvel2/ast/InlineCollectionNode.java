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

package net.simpleframework.lib.org.mvel2.ast;

import static net.simpleframework.lib.org.mvel2.util.ParseTools.findClass;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.getBaseComponentType;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.getSubComponentType;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.repeatChar;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.MVEL;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.optimizers.AccessorOptimizer;
import net.simpleframework.lib.org.mvel2.optimizers.OptimizerFactory;
import net.simpleframework.lib.org.mvel2.util.CollectionParser;

/**
 * @author Christopher Brock
 */
public class InlineCollectionNode extends ASTNode {
	private Object collectionGraph;
	int trailingStart;
	int trailingOffset;

	public InlineCollectionNode(final char[] expr, final int start, final int end, final int fields,
			final ParserContext pctx) {
		super(expr, start, end, fields | INLINE_COLLECTION, pctx);

		if ((fields & COMPILE_IMMEDIATE) != 0) {
			parseGraph(true, null, pctx);
			try {
				final AccessorOptimizer ao = OptimizerFactory.getThreadAccessorOptimizer();
				accessor = ao.optimizeCollection(pctx, collectionGraph, egressType, expr,
						trailingStart, trailingOffset, null, null, null);
				egressType = ao.getEgressType();
			} finally {
				OptimizerFactory.clearThreadAccessorOptimizer();
			}
		}
	}

	public InlineCollectionNode(final char[] expr, final int start, final int end, final int fields,
			final Class type, final ParserContext pctx) {
		super(expr, start, end, fields | INLINE_COLLECTION, pctx);

		this.egressType = type;

		if ((fields & COMPILE_IMMEDIATE) != 0) {
			try {
				parseGraph(true, type, pctx);
				final AccessorOptimizer ao = OptimizerFactory.getThreadAccessorOptimizer();
				accessor = ao.optimizeCollection(pctx, collectionGraph, egressType, expr,
						this.trailingStart, trailingOffset, null, null, null);
				egressType = ao.getEgressType();
			} finally {
				OptimizerFactory.clearThreadAccessorOptimizer();
			}
		}
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		if (accessor != null) {
			return accessor.getValue(ctx, thisValue, factory);
		} else {
			try {
				final AccessorOptimizer ao = OptimizerFactory.getThreadAccessorOptimizer();
				if (collectionGraph == null) {
					parseGraph(true, null, null);
				}

				accessor = ao.optimizeCollection(pCtx, collectionGraph, egressType, expr,
						trailingStart, trailingOffset, ctx, thisValue, factory);
				egressType = ao.getEgressType();

				return accessor.getValue(ctx, thisValue, factory);
			} finally {
				OptimizerFactory.clearThreadAccessorOptimizer();
			}
		}

	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		parseGraph(false, egressType, pCtx);

		return execGraph(collectionGraph, egressType, ctx, factory);
	}

	private void parseGraph(final boolean compile, final Class type, final ParserContext pCtx) {
		final CollectionParser parser = new CollectionParser();

		if (type == null) {
			collectionGraph = ((List) parser.parseCollection(expr, start, offset, compile, pCtx))
					.get(0);
		} else {
			collectionGraph = ((List) parser.parseCollection(expr, start, offset, compile, type, pCtx))
					.get(0);
		}

		trailingStart = parser.getCursor() + 2;
		trailingOffset = offset - (trailingStart - start);

		if (this.egressType == null) {
			this.egressType = collectionGraph.getClass();
		}
	}

	private Object execGraph(final Object o, Class type, final Object ctx,
			final VariableResolverFactory factory) {
		if (o instanceof List) {
			final ArrayList list = new ArrayList(((List) o).size());

			for (final Object item : (List) o) {
				list.add(execGraph(item, type, ctx, factory));
			}

			return list;
		} else if (o instanceof Map) {
			final HashMap map = new HashMap();

			for (final Object item : ((Map) o).keySet()) {
				map.put(execGraph(item, type, ctx, factory),
						execGraph(((Map) o).get(item), type, ctx, factory));
			}

			return map;
		} else if (o instanceof Object[]) {
			int dim = 0;

			if (type != null) {
				final String nm = type.getName();
				while (nm.charAt(dim) == '[') {
					dim++;
				}
			} else {
				type = Object[].class;
				dim = 1;
			}

			final Object newArray = Array
					.newInstance(getSubComponentType(type), ((Object[]) o).length);

			try {
				final Class cls = dim > 1 ? findClass(null, repeatChar('[', dim - 1) + "L"
						+ getBaseComponentType(type).getName() + ";", pCtx) : type;

				int c = 0;
				for (final Object item : (Object[]) o) {
					Array.set(newArray, c++, execGraph(item, cls, ctx, factory));
				}

				return newArray;
			} catch (final IllegalArgumentException e) {
				throw new CompileException("type mismatch in array", expr, start, e);
			} catch (final ClassNotFoundException e) {
				throw new RuntimeException("this error should never throw:"
						+ getBaseComponentType(type).getName(), e);
			}
		} else {
			if (type.isArray()) {
				return MVEL.eval((String) o, ctx, factory, getBaseComponentType(type));
			} else {
				return MVEL.eval((String) o, ctx, factory);
			}
		}
	}
}

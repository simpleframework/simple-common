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

import static net.simpleframework.lib.org.mvel2.util.CompilerTools.expectType;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.isJunct;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.isWhitespace;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subCompileExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.DefaultLocalVariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.ItemResolverFactory;

public class Fold extends ASTNode {
	private final ExecutableStatement subEx;
	private ExecutableStatement dataEx;
	private ExecutableStatement constraintEx;

	public Fold(final char[] expr, final int start, final int offset, final int fields,
			final ParserContext pCtx) {
		super(pCtx);
		this.expr = expr;
		this.start = start;
		this.offset = offset;

		int cursor = start;
		final int end = start + offset;
		for (; cursor < end; cursor++) {
			if (isWhitespace(expr[cursor])) {
				while (cursor < end && isWhitespace(expr[cursor])) {
					cursor++;
				}

				if (expr[cursor] == 'i' && expr[cursor + 1] == 'n' && isJunct(expr[cursor + 2])) {
					break;
				}
			}
		}

		subEx = (ExecutableStatement) subCompileExpression(expr, start, cursor - start - 1, pCtx);
		final int st = cursor += 2; // skip 'in'

		for (; cursor < end; cursor++) {
			if (isWhitespace(expr[cursor])) {
				while (cursor < end && isWhitespace(expr[cursor])) {
					cursor++;
				}

				if (expr[cursor] == 'i' && expr[cursor + 1] == 'f' && isJunct(expr[cursor + 2])) {
					final int s = cursor + 2;
					constraintEx = (ExecutableStatement) subCompileExpression(expr, s, end - s, pCtx);
					break;
				}
			}
		}

		while (isWhitespace(expr[cursor])) {
			cursor--;
		}

		expectType(pCtx,
				dataEx = (ExecutableStatement) subCompileExpression(expr, st, cursor - st, pCtx),
				Collection.class, ((fields & COMPILE_IMMEDIATE) != 0));
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		final ItemResolverFactory.ItemResolver itemR = new ItemResolverFactory.ItemResolver("$");
		final ItemResolverFactory itemFactory = new ItemResolverFactory(itemR,
				new DefaultLocalVariableResolverFactory(factory));

		List list;

		if (constraintEx != null) {
			final Collection col = ((Collection) dataEx.getValue(ctx, thisValue, factory));
			list = new ArrayList(col.size());

			for (final Object o : col) {
				itemR.value = o;
				if ((Boolean) constraintEx.getValue(ctx, thisValue, itemFactory)) {
					list.add(subEx.getValue(o, thisValue, itemFactory));
				}
			}

		} else {
			final Collection col = ((Collection) dataEx.getValue(ctx, thisValue, factory));
			list = new ArrayList(col.size());
			for (final Object o : col) {
				list.add(subEx.getValue(itemR.value = o, thisValue, itemFactory));
			}
		}
		return list;
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		final ItemResolverFactory.ItemResolver itemR = new ItemResolverFactory.ItemResolver("$");
		final ItemResolverFactory itemFactory = new ItemResolverFactory(itemR,
				new DefaultLocalVariableResolverFactory(factory));

		List list;

		if (constraintEx != null) {
			final Object x = dataEx.getValue(ctx, thisValue, factory);

			if (!(x instanceof Collection)) {
				throw new CompileException("was expecting type: Collection; but found type: "
						+ (x == null ? "null" : x.getClass().getName()), expr, start);
			}

			list = new ArrayList(((Collection) x).size());
			for (final Object o : (Collection) x) {
				itemR.value = o;
				if ((Boolean) constraintEx.getValue(ctx, thisValue, itemFactory)) {
					list.add(subEx.getValue(o, thisValue, itemFactory));
				}
			}
		} else {
			final Object x = dataEx.getValue(ctx, thisValue, factory);

			if (!(x instanceof Collection)) {
				throw new CompileException("was expecting type: Collection; but found type: "
						+ (x == null ? "null" : x.getClass().getName()), expr, start);
			}

			list = new ArrayList(((Collection) x).size());
			for (final Object o : (Collection) x) {
				list.add(subEx.getValue(itemR.value = o, thisValue, itemFactory));
			}
		}

		return list;
	}

	@Override
	public Class getEgressType() {
		return Collection.class;
	}
}

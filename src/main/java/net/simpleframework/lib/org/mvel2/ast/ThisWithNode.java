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

import static net.simpleframework.lib.org.mvel2.MVEL.executeSetExpression;
import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Christopher Brock
 */
public class ThisWithNode extends WithNode {

	public ThisWithNode(final char[] expr, final int start, final int offset, final int blockStart,
			final int blockOffset, final int fields, final ParserContext pCtx) {
		super(expr, start, offset, blockStart, blockOffset, fields, pCtx);
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		if (thisValue == null) {
			throw new CompileException("with-block against null pointer (this)", expr, start);
		}

		for (final ParmValuePair pvp : withExpressions) {
			if (pvp.getSetExpression() != null) {
				executeSetExpression(pvp.getSetExpression(), thisValue, factory, pvp.getStatement()
						.getValue(ctx, thisValue, factory));
			} else {
				pvp.getStatement().getValue(thisValue, thisValue, factory);
			}
		}
		return thisValue;
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return getReducedValueAccelerated(ctx, thisValue, factory);
	}
}
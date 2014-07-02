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

import static net.simpleframework.lib.org.mvel2.DataConversion.canConvert;
import static net.simpleframework.lib.org.mvel2.DataConversion.convert;
import static net.simpleframework.lib.org.mvel2.MVEL.eval;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subCompileExpression;
import static net.simpleframework.lib.org.mvel2.util.ReflectionUtil.isAssignableFrom;
import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class TypeCast extends ASTNode {
	private ExecutableStatement statement;
	private boolean widen;

	public TypeCast(final char[] expr, final int start, final int offset, final Class cast,
			final int fields, final ParserContext pCtx) {
		super(pCtx);
		this.egressType = cast;
		this.expr = expr;
		this.start = start;
		this.offset = offset;

		if ((fields & COMPILE_IMMEDIATE) != 0) {

			if ((statement = (ExecutableStatement) subCompileExpression(expr, start, offset, pCtx))
					.getKnownEgressType() != Object.class
					&& !canConvert(cast, statement.getKnownEgressType())) {

				if (canCast(statement.getKnownEgressType(), cast)) {
					widen = true;
				} else {
					throw new CompileException("unable to cast type: " + statement.getKnownEgressType()
							+ "; to: " + cast, expr, start);
				}
			}
		}
	}

	private boolean canCast(final Class from, final Class to) {
		return isAssignableFrom(from, to) || (from.isInterface() && interfaceAssignable(from, to));
	}

	private boolean interfaceAssignable(final Class from, final Class to) {
		for (final Class c : from.getInterfaces()) {
			if (c.isAssignableFrom(to)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		// noinspection unchecked
		return widen ? typeCheck(statement.getValue(ctx, thisValue, factory), egressType) : convert(
				statement.getValue(ctx, thisValue, factory), egressType);
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		// noinspection unchecked
		return widen ? typeCheck(eval(expr, start, offset, ctx, factory), egressType) : convert(
				eval(expr, start, offset, ctx, factory), egressType);
	}

	private static Object typeCheck(final Object inst, final Class type) {
		if (inst == null) {
			return null;
		}
		if (type.isInstance(inst)) {
			return inst;
		} else {
			throw new ClassCastException(inst.getClass().getName() + " cannot be cast to: "
					+ type.getClass().getName());
		}
	}

	public ExecutableStatement getStatement() {
		return statement;
	}
}

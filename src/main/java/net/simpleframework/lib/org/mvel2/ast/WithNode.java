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

import static net.simpleframework.lib.org.mvel2.util.ParseTools.balancedCapture;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.createShortFormOperativeAssignment;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.createStringTrimmed;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.opLookup;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.parseWithExpressions;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subCompileExpression;
import static net.simpleframework.lib.org.mvel2.util.PropertyTools.getReturnType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.MVEL;
import net.simpleframework.lib.org.mvel2.Operator;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.util.ErrorUtil;

/**
 * @author Christopher Brock
 */
public class WithNode extends BlockNode implements NestedStatement {
	protected String nestParm;
	// protected ExecutableStatement nestedStatement;
	protected ParmValuePair[] withExpressions;

	public WithNode(final char[] expr, final int start, final int offset, final int blockStart,
			final int blockOffset, final int fields, final ParserContext pCtx) {
		super(pCtx);
		nestParm = createStringTrimmed(this.expr = expr, this.start = start, this.offset = offset);
		this.blockStart = blockStart;
		this.blockOffset = blockOffset;

		if ((fields & COMPILE_IMMEDIATE) != 0) {
			pCtx.setBlockSymbols(true);

			egressType = (compiledBlock = (ExecutableStatement) subCompileExpression(expr, start,
					offset, pCtx)).getKnownEgressType();

			withExpressions = compileWithExpressions(expr, blockStart, blockOffset, nestParm,
					egressType, pCtx);

			pCtx.setBlockSymbols(false);
		}
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		final Object ctxObject = compiledBlock.getValue(ctx, thisValue, factory);
		if (ctxObject == null) {
			throw new CompileException("with-block against null pointer", expr, start);
		}

		for (final ParmValuePair pvp : withExpressions) {
			pvp.eval(ctxObject, factory);
		}

		return ctxObject;
	}

	@Override
	public Object getReducedValue(Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		parseWithExpressions(nestParm, expr, blockStart, blockOffset,
				ctx = MVEL.eval(expr, start, offset, ctx, factory), factory);
		return ctx;
	}

	public static ParmValuePair[] compileWithExpressions(final char[] block, final int start,
			final int offset, final String nestParm, final Class egressType,
			final ParserContext pCtx) {
		/**
		 *
		 * MAINTENANCE NOTE: AN INTERPRETED MODE VERSION OF THIS CODE IS
		 * DUPLICATED IN: ParseTools
		 *
		 */

		final List<ParmValuePair> parms = new ArrayList<ParmValuePair>();
		String parm = "";

		final int end = start + offset;

		int _st = start;
		int _end = -1;

		int oper = -1;
		for (int i = start; i < end; i++) {
			switch (block[i]) {
			case '{':
			case '[':
			case '(':
			case '\'':
			case '"':
				i = balancedCapture(block, i, end, block[i]);
				continue;

			case '/':
				if (i < end && block[i + 1] == '/') {
					while (i < end && block[i] != '\n') {
						block[i++] = ' ';
					}
					if (parm == null) {
						_st = i;
					}
				} else if (i < end && block[i + 1] == '*') {
					final int len = end - 1;
					while (i < len && !(block[i] == '*' && block[i + 1] == '/')) {
						block[i++] = ' ';
					}
					block[i++] = ' ';
					block[i++] = ' ';

					if (parm == null) {
						_st = i;
					}
				} else if (i < end && block[i + 1] == '=') {
					oper = Operator.DIV;
				}
				continue;

			case '%':
			case '*':
			case '-':
			case '+':
				if (i + 1 < end && block[i + 1] == '=') {
					oper = opLookup(block[i]);
				}
				continue;

			case '=':
				parm = createStringTrimmed(block, _st, i - _st - (oper != -1 ? 1 : 0));
				_st = i + 1;
				continue;

			case ',':
				if (_end == -1) {
					_end = i;
				}

				if (parm == null || parm.length() == 0) {
					try {
						String expr;
						if (nestParm == null) {
							expr = new String(block, _st, _end - _st);
						} else {
							expr = new StringBuilder(nestParm).append('.')
									.append(new String(block, _st, _end - _st)).toString();
						}

						parms.add(new ParmValuePair(null,
								(ExecutableStatement) subCompileExpression(expr, pCtx), egressType, pCtx));

					} catch (final CompileException e) {
						e.setCursor(_st + (e.getCursor() - (e.getExpr().length - offset)));
						e.setExpr(block);
						throw e;
					}

					oper = -1;
					_st = ++i;
				} else {
					if (nestParm == null) {
						throw new CompileException("operative assignment not possible here", block,
								start);
					}

					try {
						parms.add(new ParmValuePair(parm, oper != -1
								? (ExecutableStatement) subCompileExpression(
										createShortFormOperativeAssignment(nestParm + "." + parm, block, _st,
												_end - _st, oper),
										pCtx)
								// or
								: (ExecutableStatement) subCompileExpression(block, _st, _end - _st, pCtx),
								egressType, pCtx));
					} catch (final CompileException e) {
						e.setCursor(_st + (e.getCursor() - (e.getExpr().length - offset)));
						e.setExpr(block);
						throw e;
					}

					parm = null;
					oper = -1;
					_st = ++i;
				}

				_end = -1;

				break;
			}
		}

		if (_st != (_end = end)) {
			try {
				if (parm == null || "".equals(parm)) {
					String expr;
					if (nestParm == null) {
						expr = new String(block, _st, _end - _st);
					} else {
						expr = new StringBuilder(nestParm).append('.')
								.append(new String(block, _st, _end - _st)).toString();
					}

					parms.add(new ParmValuePair(null,
							(ExecutableStatement) subCompileExpression(expr, pCtx), egressType, pCtx));
				} else {
					if (nestParm == null) {
						throw new CompileException("operative assignment not possible here", block,
								start);
					}

					parms.add(new ParmValuePair(parm, oper != -1
							? (ExecutableStatement) subCompileExpression(
									createShortFormOperativeAssignment(nestParm + "." + parm, block, _st,
											_end - _st, oper),
									pCtx)
							// or
							: (ExecutableStatement) subCompileExpression(block, _st, _end - _st, pCtx),
							egressType, pCtx));
				}
			} catch (final CompileException e) {
				throw ErrorUtil.rewriteIfNeeded(e, block, _st);
			}
		}

		ParmValuePair[] withExpressions;
		parms.toArray(withExpressions = new ParmValuePair[parms.size()]);
		return withExpressions;
	}

	@Override
	public ExecutableStatement getNestedStatement() {
		return compiledBlock;
	}

	public ParmValuePair[] getWithExpressions() {
		return withExpressions;
	}

	public static final class ParmValuePair implements Serializable {
		private Serializable setExpression;
		private final ExecutableStatement statement;

		public ParmValuePair(final String parameter, final ExecutableStatement statement,
				final Class ingressType, final ParserContext pCtx) {
			if (parameter != null && parameter.length() != 0) {
				this.setExpression = MVEL.compileSetExpression(parameter,
						ingressType != null ? getReturnType(ingressType, parameter, pCtx) : Object.class,
						pCtx);
			}
			this.statement = statement;
		}

		public Serializable getSetExpression() {
			return setExpression;
		}

		public ExecutableStatement getStatement() {
			return statement;
		}

		public void eval(final Object ctx, final VariableResolverFactory factory) {
			if (setExpression == null) {
				this.statement.getValue(ctx, factory);
			} else {
				MVEL.executeSetExpression(setExpression, ctx, factory,
						this.statement.getValue(ctx, factory));
			}
		}
	}
}

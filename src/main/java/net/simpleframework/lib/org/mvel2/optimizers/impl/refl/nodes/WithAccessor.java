package net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes;

import static net.simpleframework.lib.org.mvel2.MVEL.executeSetExpression;
import static net.simpleframework.lib.org.mvel2.util.PropertyTools.getReturnType;

import java.io.Serializable;

import net.simpleframework.lib.org.mvel2.MVEL;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.ast.WithNode;
import net.simpleframework.lib.org.mvel2.compiler.AccessorNode;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class WithAccessor implements AccessorNode {
	private AccessorNode nextNode;

	protected String nestParm;
	protected ExecutableStatement nestedStatement;
	protected WithNode.ParmValuePair[] withExpressions;

	public WithAccessor(final ParserContext pCtx, final String property, final char[] expr,
			final int start, final int offset, final Class ingressType) {
		pCtx.setBlockSymbols(true);

		withExpressions = WithNode.compileWithExpressions(expr, start, offset, property, ingressType,
				pCtx);

		pCtx.setBlockSymbols(false);
	}

	@Override
	public AccessorNode getNextNode() {
		return this.nextNode;
	}

	@Override
	public AccessorNode setNextNode(final AccessorNode accessorNode) {
		return this.nextNode = accessorNode;
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) {
		if (this.nextNode == null) {
			return processWith(ctx, elCtx, variableFactory);
		} else {
			return this.nextNode.getValue(processWith(ctx, elCtx, variableFactory), elCtx,
					variableFactory);
		}
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		return this.nextNode.setValue(processWith(ctx, elCtx, variableFactory), elCtx,
				variableFactory, value);
	}

	public Object processWith(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		for (final WithNode.ParmValuePair pvp : withExpressions) {
			if (pvp.getSetExpression() != null) {
				executeSetExpression(pvp.getSetExpression(), ctx, factory,
						pvp.getStatement().getValue(ctx, thisValue, factory));
			} else {
				pvp.getStatement().getValue(ctx, thisValue, factory);
			}
		}

		return ctx;
	}

	public static final class ExecutablePairs implements Serializable {
		private Serializable setExpression;
		private ExecutableStatement statement;

		public ExecutablePairs() {
		}

		public ExecutablePairs(final String parameter, final ExecutableStatement statement,
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

		public void setSetExpression(final Serializable setExpression) {
			this.setExpression = setExpression;
		}

		public ExecutableStatement getStatement() {
			return statement;
		}

		public void setStatement(final ExecutableStatement statement) {
			this.statement = statement;
		}
	}

	@Override
	public Class getKnownEgressType() {
		return Object.class;
	}
}

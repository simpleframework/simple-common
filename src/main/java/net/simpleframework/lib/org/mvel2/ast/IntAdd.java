package net.simpleframework.lib.org.mvel2.ast;

import net.simpleframework.lib.org.mvel2.Operator;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class IntAdd extends BinaryOperation implements IntOptimized {

	public IntAdd(final ASTNode left, final ASTNode right, final ParserContext pCtx) {
		super(Operator.ADD, pCtx);
		this.left = left;
		this.right = right;
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return ((Integer) left.getReducedValueAccelerated(ctx, thisValue, factory))
				+ ((Integer) right.getReducedValueAccelerated(ctx, thisValue, factory));
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return ((Integer) left.getReducedValue(ctx, thisValue, factory))
				+ ((Integer) right.getReducedValue(ctx, thisValue, factory));
	}

	@Override
	public Class getEgressType() {
		return Integer.class;
	}
}

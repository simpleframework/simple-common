package net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes;

import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.Accessor;
import net.simpleframework.lib.org.mvel2.compiler.AccessorNode;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.optimizers.OptimizerFactory;

public class NullSafe implements AccessorNode {
	private AccessorNode nextNode;
	private final char[] expr;
	private final int start;
	private final int offset;
	private final ParserContext pCtx;

	public NullSafe(final char[] expr, final int start, final int offset, final ParserContext pCtx) {
		this.expr = expr;
		this.start = start;
		this.offset = offset;
		this.pCtx = pCtx;
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) {
		if (ctx == null) {
			return null;
		}
		if (nextNode == null) {
			final Accessor a = OptimizerFactory.getAccessorCompiler(OptimizerFactory.SAFE_REFLECTIVE)
					.optimizeAccessor(pCtx, expr, start, offset, ctx, elCtx, variableFactory, true,
							ctx.getClass());

			nextNode = new AccessorNode() {
				@Override
				public AccessorNode getNextNode() {
					return null;
				}

				@Override
				public AccessorNode setNextNode(final AccessorNode accessorNode) {
					return null;
				}

				@Override
				public Object getValue(final Object ctx, final Object elCtx,
						final VariableResolverFactory variableFactory) {
					return a.getValue(ctx, elCtx, variableFactory);
				}

				@Override
				public Object setValue(final Object ctx, final Object elCtx,
						final VariableResolverFactory variableFactory, final Object value) {
					return a.setValue(ctx, elCtx, variableFactory, value);
				}

				@Override
				public Class getKnownEgressType() {
					return a.getKnownEgressType();
				}
			};

		}
		// else {
		return nextNode.getValue(ctx, elCtx, variableFactory);
		// }
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		if (ctx == null) {
			return null;
		}
		return nextNode.setValue(ctx, elCtx, variableFactory, value);
	}

	@Override
	public AccessorNode getNextNode() {
		return nextNode;
	}

	@Override
	public AccessorNode setNextNode(final AccessorNode accessorNode) {
		return this.nextNode = accessorNode;
	}

	@Override
	public Class getKnownEgressType() {
		return Object.class;
	}
}

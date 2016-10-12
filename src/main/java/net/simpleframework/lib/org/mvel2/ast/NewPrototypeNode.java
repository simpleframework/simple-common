package net.simpleframework.lib.org.mvel2.ast;

import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class NewPrototypeNode extends ASTNode {
	private final String protoName;

	public NewPrototypeNode(final TypeDescriptor t, final ParserContext pCtx) {
		super(pCtx);
		this.protoName = t.getClassName();
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return ((Proto) factory.getVariableResolver(protoName).getValue()).newInstance(ctx, thisValue,
				factory);
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return ((Proto) factory.getVariableResolver(protoName).getValue()).newInstance(ctx, thisValue,
				factory);
	}
}

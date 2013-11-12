package net.simpleframework.lib.org.mvel2.ast;

import java.util.HashMap;

import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.MapVariableResolverFactory;

/**
 * @author Mike Brock
 */
public class NewObjectPrototype extends ASTNode {
	private final Function function;

	public NewObjectPrototype(final ParserContext pCtx, final Function function) {
		super(pCtx);
		this.function = function;
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		final MapVariableResolverFactory resolverFactory = new MapVariableResolverFactory(
				new HashMap<String, Object>(), factory);
		function.getCompiledBlock().getValue(ctx, thisValue, resolverFactory);
		return new PrototypalFunctionInstance(function, resolverFactory);
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return getReducedValue(ctx, thisValue, factory);
	}
}

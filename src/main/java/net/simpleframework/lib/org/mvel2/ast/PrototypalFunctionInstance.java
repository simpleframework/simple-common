package net.simpleframework.lib.org.mvel2.ast;

import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Mike Brock
 */
public class PrototypalFunctionInstance extends FunctionInstance {
	private final VariableResolverFactory resolverFactory;

	public PrototypalFunctionInstance(final Function function,
			final VariableResolverFactory resolverFactory) {
		super(function);
		this.resolverFactory = resolverFactory;
	}

	@Override
	public Object call(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory, final Object[] parms) {
		return function.call(ctx, thisValue, new InvokationContextFactory(factory, resolverFactory),
				parms);
	}

	public VariableResolverFactory getResolverFactory() {
		return resolverFactory;
	}

	@Override
	public String toString() {
		return "function_prototype:" + function.getName();
	}

}

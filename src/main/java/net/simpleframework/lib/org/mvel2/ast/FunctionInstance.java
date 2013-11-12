package net.simpleframework.lib.org.mvel2.ast;

import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Mike Brock
 */
public class FunctionInstance {
	protected final Function function;

	public FunctionInstance(final Function function) {
		this.function = function;
	}

	public Function getFunction() {
		return function;
	}

	public Object call(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory, final Object[] parms) {
		return function.call(ctx, thisValue, factory, parms);
	}
}

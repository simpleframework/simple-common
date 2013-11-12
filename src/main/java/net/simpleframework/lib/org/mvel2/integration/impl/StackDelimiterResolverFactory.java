package net.simpleframework.lib.org.mvel2.integration.impl;

import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Mike Brock
 */
public class StackDelimiterResolverFactory extends StackDemarcResolverFactory {
	public StackDelimiterResolverFactory(final VariableResolverFactory delegate) {
		super(delegate);
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value) {
		final VariableResolverFactory delegate = getDelegate();
		final VariableResolverFactory nextFactory = delegate.getNextFactory();
		delegate.setNextFactory(null);
		final VariableResolver resolver = delegate.createVariable(name, value);
		delegate.setNextFactory(nextFactory);
		return resolver;
	}
}

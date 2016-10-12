package net.simpleframework.lib.org.mvel2.integration.impl;

import java.util.Set;

import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Mike Brock
 */
public class StackResetResolverFactory implements VariableResolverFactory {
	private final VariableResolverFactory delegate;

	public StackResetResolverFactory(final VariableResolverFactory delegate) {
		delegate.setTiltFlag(false);
		this.delegate = delegate;
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value) {
		return delegate.createVariable(name, value);
	}

	@Override
	public VariableResolver createIndexedVariable(final int index, final String name,
			final Object value) {
		return delegate.createIndexedVariable(index, name, value);
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value,
			final Class<?> type) {
		return delegate.createVariable(name, value, type);
	}

	@Override
	public VariableResolver createIndexedVariable(final int index, final String name,
			final Object value, final Class<?> typee) {
		return delegate.createIndexedVariable(index, name, value, typee);
	}

	@Override
	public VariableResolver setIndexedVariableResolver(final int index,
			final VariableResolver variableResolver) {
		return delegate.setIndexedVariableResolver(index, variableResolver);
	}

	@Override
	public VariableResolverFactory getNextFactory() {
		return delegate.getNextFactory();
	}

	@Override
	public VariableResolverFactory setNextFactory(final VariableResolverFactory resolverFactory) {
		return delegate.setNextFactory(resolverFactory);
	}

	@Override
	public VariableResolver getVariableResolver(final String name) {
		return delegate.getVariableResolver(name);
	}

	@Override
	public VariableResolver getIndexedVariableResolver(final int index) {
		return delegate.getIndexedVariableResolver(index);
	}

	@Override
	public boolean isTarget(final String name) {
		return delegate.isTarget(name);
	}

	@Override
	public boolean isResolveable(final String name) {
		return delegate.isResolveable(name);
	}

	@Override
	public Set<String> getKnownVariables() {
		return delegate.getKnownVariables();
	}

	@Override
	public int variableIndexOf(final String name) {
		return delegate.variableIndexOf(name);
	}

	@Override
	public boolean isIndexedFactory() {
		return delegate.isIndexedFactory();
	}

	@Override
	public boolean tiltFlag() {
		return delegate.tiltFlag();
	}

	@Override
	public void setTiltFlag(final boolean tilt) {
		if (!delegate.tiltFlag()) {
			delegate.setTiltFlag(tilt);
		}
	}

	public VariableResolverFactory getDelegate() {
		return delegate;
	}
}

package net.simpleframework.lib.org.mvel2.integration.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.simpleframework.lib.org.mvel2.integration.VariableResolver;

public class SimpleVariableResolverFactory extends BaseVariableResolverFactory {
	public SimpleVariableResolverFactory(final Map<String, Object> variables) {
		for (final Map.Entry<String, Object> entry : variables.entrySet()) {
			createVariable(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value) {
		if (variableResolvers == null) {
			variableResolvers = new HashMap<>(5, 0.6f);
		}
		final SimpleValueResolver svr = new SimpleValueResolver(value);
		variableResolvers.put(name, svr);
		return svr;
	}

	@Override
	public VariableResolver createIndexedVariable(final int index, final String name,
			final Object value) {
		return null;
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value,
			final Class<?> type) {
		if (variableResolvers == null) {
			variableResolvers = new HashMap<>(5, 0.6f);
		}
		final SimpleSTValueResolver svr = new SimpleSTValueResolver(value, type);
		variableResolvers.put(name, svr);
		return svr;
	}

	@Override
	public VariableResolver createIndexedVariable(final int index, final String name,
			final Object value, final Class<?> typee) {
		return null;
	}

	@Override
	public VariableResolver setIndexedVariableResolver(final int index,
			final VariableResolver variableResolver) {
		return null;
	}

	@Override
	public boolean isTarget(final String name) {
		return variableResolvers.containsKey(name);
	}

	@Override
	public boolean isResolveable(final String name) {
		return variableResolvers.containsKey(name)
				|| (nextFactory != null && nextFactory.isResolveable(name));
	}

	@Override
	public VariableResolver getVariableResolver(final String name) {
		final VariableResolver vr = variableResolvers.get(name);
		return vr != null ? vr : (nextFactory == null ? null : nextFactory.getVariableResolver(name));
	}

	@Override
	public Set<String> getKnownVariables() {
		return variableResolvers.keySet();
	}

	@Override
	public int variableIndexOf(final String name) {
		return 0; // To change body of implemented methods use File | Settings |
						// File Templates.
	}

	@Override
	public boolean isIndexedFactory() {
		return false; // To change body of implemented methods use File | Settings
							// | File Templates.
	}
}

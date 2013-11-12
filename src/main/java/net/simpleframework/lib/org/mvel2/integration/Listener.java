package net.simpleframework.lib.org.mvel2.integration;

public interface Listener {
	public void onEvent(Object context, String contextName, VariableResolverFactory variableFactory,
			Object value);
}

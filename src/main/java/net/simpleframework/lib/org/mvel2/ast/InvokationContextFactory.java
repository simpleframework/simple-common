package net.simpleframework.lib.org.mvel2.ast;

import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.MapVariableResolverFactory;

/**
 * @author Mike Brock
 */
public class InvokationContextFactory extends MapVariableResolverFactory {
	private final VariableResolverFactory protoContext;

	public InvokationContextFactory(final VariableResolverFactory next,
			final VariableResolverFactory protoContext) {
		this.nextFactory = next;
		this.protoContext = protoContext;
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value) {
		if (isResolveable(name) && !protoContext.isResolveable(name)) {
			return nextFactory.createVariable(name, value);
		} else {
			return protoContext.createVariable(name, value);
		}
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value, final Class<?> type) {
		if (isResolveable(name) && !protoContext.isResolveable(name)) {
			return nextFactory.createVariable(name, value, type);
		} else {
			return protoContext.createVariable(name, value, type);
		}
	}

	@Override
	public VariableResolver getVariableResolver(final String name) {
		if (isResolveable(name) && !protoContext.isResolveable(name)) {
			return nextFactory.getVariableResolver(name);
		} else {
			return protoContext.getVariableResolver(name);
		}
	}

	@Override
	public boolean isTarget(final String name) {
		return protoContext.isTarget(name);
	}

	@Override
	public boolean isResolveable(final String name) {
		return protoContext.isResolveable(name) || nextFactory.isResolveable(name);
	}

	@Override
	public boolean isIndexedFactory() {
		return true;
	}
}

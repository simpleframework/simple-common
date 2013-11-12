package net.simpleframework.lib.org.mvel2.util;

import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public interface CallableProxy {
	public Object call(Object ctx, Object thisCtx, VariableResolverFactory factory,
			Object[] parameters);
}

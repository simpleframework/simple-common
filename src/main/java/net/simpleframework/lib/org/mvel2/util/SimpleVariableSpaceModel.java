package net.simpleframework.lib.org.mvel2.util;

import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.IndexVariableResolver;
import net.simpleframework.lib.org.mvel2.integration.impl.IndexedVariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.SimpleValueResolver;

/**
 * @author Mike Brock .
 */
public class SimpleVariableSpaceModel extends VariableSpaceModel {
	public SimpleVariableSpaceModel(final String[] varNames) {
		this.allVars = varNames;
	}

	public VariableResolverFactory createFactory(final Object[] vals) {
		final VariableResolver[] resolvers = new VariableResolver[allVars.length];
		for (int i = 0; i < resolvers.length; i++) {
			if (i >= vals.length) {
				resolvers[i] = new SimpleValueResolver(null);
			} else {
				resolvers[i] = new IndexVariableResolver(i, vals);
			}
		}

		return new IndexedVariableResolverFactory(allVars, resolvers);
	}
}

/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.simpleframework.lib.org.mvel2.integration.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.simpleframework.lib.org.mvel2.UnresolveablePropertyException;
import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * Use this class to extend you own VariableResolverFactories. It contains most
 * of the baseline implementation needed
 * for the vast majority of integration needs.
 */
public abstract class BaseVariableResolverFactory implements VariableResolverFactory {
	protected Map<String, VariableResolver> variableResolvers = new HashMap<>();
	protected VariableResolverFactory nextFactory;

	protected int indexOffset = 0;
	protected String[] indexedVariableNames;
	protected VariableResolver[] indexedVariableResolvers;

	private boolean tiltFlag;

	@Override
	public VariableResolverFactory getNextFactory() {
		return nextFactory;
	}

	@Override
	public VariableResolverFactory setNextFactory(final VariableResolverFactory resolverFactory) {
		return nextFactory = resolverFactory;
	}

	@Override
	public VariableResolver getVariableResolver(final String name) {
		if (isResolveable(name)) {
			if (variableResolvers.containsKey(name)) {
				return variableResolvers.get(name);
			} else if (nextFactory != null) {
				return nextFactory.getVariableResolver(name);
			}
		}

		throw new UnresolveablePropertyException("unable to resolve variable '" + name + "'");
	}

	public boolean isNextResolveable(final String name) {
		return nextFactory != null && nextFactory.isResolveable(name);
	}

	public void appendFactory(final VariableResolverFactory resolverFactory) {
		if (nextFactory == null) {
			nextFactory = resolverFactory;
		} else {
			VariableResolverFactory vrf = nextFactory;
			while (vrf.getNextFactory() != null) {
				vrf = vrf.getNextFactory();
			}
			vrf.setNextFactory(nextFactory);
		}
	}

	public void insertFactory(final VariableResolverFactory resolverFactory) {
		if (nextFactory == null) {
			nextFactory = resolverFactory;
		} else {
			resolverFactory.setNextFactory(nextFactory = resolverFactory);
		}
	}

	@Override
	public Set<String> getKnownVariables() {
		if (nextFactory == null) {
			return new HashSet<>(variableResolvers.keySet());
			// return new HashSet<String>(0);
		} else {
			final HashSet<String> vars = new HashSet<>(variableResolvers.keySet());
			vars.addAll(nextFactory.getKnownVariables());
			return vars;
		}
	}

	@Override
	public VariableResolver createIndexedVariable(final int index, final String name,
			final Object value) {
		if (nextFactory != null) {
			return nextFactory.createIndexedVariable(index - indexOffset, name, value);
		} else {
			throw new RuntimeException("cannot create indexed variable: " + name + "(" + index
					+ "). operation not supported by resolver: " + this.getClass().getName());
		}
	}

	@Override
	public VariableResolver getIndexedVariableResolver(final int index) {
		if (nextFactory != null) {
			return nextFactory.getIndexedVariableResolver(index - indexOffset);
		} else {
			throw new RuntimeException("cannot access indexed variable: " + index
					+ ".  operation not supported by resolver: " + this.getClass().getName());
		}
	}

	@Override
	public VariableResolver createIndexedVariable(final int index, final String name,
			final Object value, final Class<?> type) {
		if (nextFactory != null) {
			return nextFactory.createIndexedVariable(index - indexOffset, name, value, type);
		} else {
			throw new RuntimeException("cannot access indexed variable: " + name + "(" + index
					+ ").  operation not supported by resolver.: " + this.getClass().getName());
		}
	}

	public Map<String, VariableResolver> getVariableResolvers() {
		return variableResolvers;
	}

	public void setVariableResolvers(final Map<String, VariableResolver> variableResolvers) {
		this.variableResolvers = variableResolvers;
	}

	public String[] getIndexedVariableNames() {
		return indexedVariableNames;
	}

	public void setIndexedVariableNames(final String[] indexedVariableNames) {
		this.indexedVariableNames = indexedVariableNames;
	}

	@Override
	public int variableIndexOf(final String name) {
		if (indexedVariableNames != null) {
			for (int i = 0; i < indexedVariableNames.length; i++) {
				if (name.equals(indexedVariableNames[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public VariableResolver setIndexedVariableResolver(final int index,
			final VariableResolver resolver) {
		if (indexedVariableResolvers == null) {
			return (indexedVariableResolvers = new VariableResolver[indexedVariableNames.length])[index
					- indexOffset] = resolver;
		} else {
			return indexedVariableResolvers[index - indexOffset] = resolver;
		}
	}

	@Override
	public boolean isIndexedFactory() {
		return false;
	}

	@Override
	public boolean tiltFlag() {
		return tiltFlag;
	}

	@Override
	public void setTiltFlag(final boolean tiltFlag) {
		this.tiltFlag = tiltFlag;
		if (nextFactory != null) {
			nextFactory.setTiltFlag(tiltFlag);
		}
	}
}

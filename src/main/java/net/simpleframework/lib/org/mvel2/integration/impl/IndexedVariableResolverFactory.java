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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.simpleframework.lib.org.mvel2.UnresolveablePropertyException;
import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class IndexedVariableResolverFactory extends BaseVariableResolverFactory {

	public IndexedVariableResolverFactory(final String[] varNames, final VariableResolver[] resolvers) {
		this.indexedVariableNames = varNames;
		this.indexedVariableResolvers = resolvers;
	}

	public IndexedVariableResolverFactory(final String[] varNames, final Object[] values) {
		this.indexedVariableNames = varNames;
		this.indexedVariableResolvers = createResolvers(values, varNames.length);
	}

	public IndexedVariableResolverFactory(final String[] varNames, final Object[] values,
			final VariableResolverFactory nextFactory) {
		this.indexedVariableNames = varNames;
		this.nextFactory = new MapVariableResolverFactory();
		this.nextFactory.setNextFactory(nextFactory);
		this.indexedVariableResolvers = createResolvers(values, varNames.length);

	}

	private static VariableResolver[] createResolvers(final Object[] values, final int size) {
		final VariableResolver[] vr = new VariableResolver[size];
		for (int i = 0; i < size; i++) {
			vr[i] = i >= values.length ? new SimpleValueResolver(null) : new IndexVariableResolver(i,
					values);
		}
		return vr;
	}

	@Override
	public VariableResolver createIndexedVariable(final int index, final String name,
			final Object value) {
		final VariableResolver r = indexedVariableResolvers[index];
		r.setValue(value);
		return r;
	}

	@Override
	public VariableResolver getIndexedVariableResolver(final int index) {
		return indexedVariableResolvers[index];
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value) {
		final VariableResolver vr = getResolver(name);
		if (vr != null) {
			vr.setValue(value);
		}
		return vr;
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value, final Class<?> type) {
		final VariableResolver vr = getResolver(name);
		if (vr != null) {
			vr.setValue(value);
		}
		return vr;

		// if (nextFactory == null) nextFactory = new
		// MapVariableResolverFactory(new HashMap());
		// return nextFactory.createVariable(name, value, type);
	}

	@Override
	public VariableResolver getVariableResolver(final String name) {
		final VariableResolver vr = getResolver(name);
		if (vr != null) {
			return vr;
		} else if (nextFactory != null) {
			return nextFactory.getVariableResolver(name);
		}

		throw new UnresolveablePropertyException("unable to resolve variable '" + name + "'");
	}

	@Override
	public boolean isResolveable(final String name) {
		return isTarget(name) || (nextFactory != null && nextFactory.isResolveable(name));
	}

	protected VariableResolver addResolver(final String name, final VariableResolver vr) {
		variableResolvers.put(name, vr);
		return vr;
	}

	private VariableResolver getResolver(final String name) {
		for (int i = 0; i < indexedVariableNames.length; i++) {
			if (indexedVariableNames[i].equals(name)) {
				return indexedVariableResolvers[i];
			}
		}
		return null;
	}

	@Override
	public boolean isTarget(final String name) {
		for (final String indexedVariableName : indexedVariableNames) {
			if (indexedVariableName.equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<String> getKnownVariables() {
		return new HashSet<String>(Arrays.asList(indexedVariableNames));
	}

	public void clear() {
		// variableResolvers.clear();

	}

	@Override
	public boolean isIndexedFactory() {
		return true;
	}
}
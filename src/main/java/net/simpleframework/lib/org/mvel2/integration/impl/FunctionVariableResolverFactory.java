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

import net.simpleframework.lib.org.mvel2.ast.Function;
import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class FunctionVariableResolverFactory extends BaseVariableResolverFactory implements
		LocalVariableResolverFactory {
	private final Function function;

	public FunctionVariableResolverFactory(final Function function,
			final VariableResolverFactory nextFactory, final String[] indexedVariables,
			final Object[] parameters) {
		this.function = function;

		this.variableResolvers = new HashMap<String, VariableResolver>();
		this.nextFactory = nextFactory;
		this.indexedVariableResolvers = new VariableResolver[(this.indexedVariableNames = indexedVariables).length];
		for (int i = 0; i < parameters.length; i++) {
			variableResolvers.put(indexedVariableNames[i], null);
			this.indexedVariableResolvers[i] = new SimpleValueResolver(parameters[i]);
			// variableResolvers.put(indexedVariableNames[i],
			// this.indexedVariableResolvers[i] = new
			// SimpleValueResolver(parameters[i]));
		}
	}

	@Override
	public boolean isResolveable(final String name) {
		return variableResolvers.containsKey(name)
				|| (nextFactory != null && nextFactory.isResolveable(name));
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value) {
		final VariableResolver resolver = getVariableResolver(name);
		if (resolver == null) {
			final int idx = increaseRegisterTableSize();
			this.indexedVariableNames[idx] = name;
			this.indexedVariableResolvers[idx] = new SimpleValueResolver(value);
			variableResolvers.put(name, null);

			// variableResolvers.put(name, this.indexedVariableResolvers[idx] = new
			// SimpleValueResolver(value));
			return this.indexedVariableResolvers[idx];
		} else {
			resolver.setValue(value);
			return resolver;
		}
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value, final Class<?> type) {
		final VariableResolver vr = this.variableResolvers != null ? this.variableResolvers.get(name)
				: null;
		if (vr != null && vr.getType() != null) {
			throw new RuntimeException("variable already defined within scope: " + vr.getType() + " "
					+ name);
		} else {
			return createIndexedVariable(variableIndexOf(name), name, value);
		}
	}

	@Override
	public VariableResolver createIndexedVariable(int index, final String name, final Object value) {
		index = index - indexOffset;
		if (indexedVariableResolvers[index] != null) {
			indexedVariableResolvers[index].setValue(value);
		} else {
			indexedVariableResolvers[index] = new SimpleValueResolver(value);
		}

		variableResolvers.put(name, null);

		return indexedVariableResolvers[index];
	}

	@Override
	public VariableResolver createIndexedVariable(int index, final String name, final Object value,
			final Class<?> type) {
		index = index - indexOffset;
		if (indexedVariableResolvers[index] != null) {
			indexedVariableResolvers[index].setValue(value);
		} else {
			indexedVariableResolvers[index] = new SimpleValueResolver(value);
		}
		return indexedVariableResolvers[index];
	}

	@Override
	public VariableResolver getIndexedVariableResolver(final int index) {
		if (indexedVariableResolvers[index] == null) {
			/**
			 * If the register is null, this means we need to forward-allocate the
			 * variable onto the register table.
			 */
			return indexedVariableResolvers[index] = super
					.getVariableResolver(indexedVariableNames[index]);
		}
		return indexedVariableResolvers[index];
	}

	@Override
	public VariableResolver getVariableResolver(final String name) {
		int idx;
		// if (variableResolvers.containsKey(name)) return
		// variableResolvers.get(name);
		if ((idx = variableIndexOf(name)) != -1) {
			if (indexedVariableResolvers[idx] == null) {
				indexedVariableResolvers[idx] = new SimpleValueResolver(null);
			}
			variableResolvers.put(indexedVariableNames[idx], null);
			return indexedVariableResolvers[idx];
		}

		return super.getVariableResolver(name);
	}

	@Override
	public boolean isIndexedFactory() {
		return true;
	}

	@Override
	public boolean isTarget(final String name) {
		return variableResolvers.containsKey(name) || variableIndexOf(name) != -1;
	}

	private int increaseRegisterTableSize() {
		final String[] oldNames = indexedVariableNames;
		final VariableResolver[] oldResolvers = indexedVariableResolvers;

		final int newLength = oldNames.length + 1;
		indexedVariableNames = new String[newLength];
		indexedVariableResolvers = new VariableResolver[newLength];

		for (int i = 0; i < oldNames.length; i++) {
			indexedVariableNames[i] = oldNames[i];
			indexedVariableResolvers[i] = oldResolvers[i];
		}

		return newLength - 1;
	}

	public void updateParameters(final Object[] parameters) {
		// this.indexedVariableResolvers = new
		// VariableResolver[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			this.indexedVariableResolvers[i] = new SimpleValueResolver(parameters[i]);
		}
		// for (int i = parameters.length; i < indexedVariableResolvers.length;
		// i++) {
		// this.indexedVariableResolvers[i] = null;
		// }
	}

	public VariableResolver[] getIndexedVariableResolvers() {
		return this.indexedVariableResolvers;
	}

	public void setIndexedVariableResolvers(final VariableResolver[] vr) {
		this.indexedVariableResolvers = vr;
	}

	public Function getFunction() {
		return function;
	}

	public void setIndexOffset(final int offset) {
		this.indexOffset = offset;
	}

	private boolean noTilt = false;

	public VariableResolverFactory setNoTilt(final boolean noTilt) {
		this.noTilt = noTilt;
		return this;
	}

	@Override
	public void setTiltFlag(final boolean tiltFlag) {
		if (!noTilt) {
			super.setTiltFlag(tiltFlag);
		}
	}
}
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
import java.util.Map;

import net.simpleframework.lib.org.mvel2.UnresolveablePropertyException;
import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class DefaultLocalVariableResolverFactory extends MapVariableResolverFactory
		implements LocalVariableResolverFactory {
	public DefaultLocalVariableResolverFactory() {
		super(new HashMap<String, Object>());
	}

	public DefaultLocalVariableResolverFactory(final Map<String, Object> variables) {
		super(variables);
	}

	public DefaultLocalVariableResolverFactory(final Map<String, Object> variables,
			final VariableResolverFactory nextFactory) {
		super(variables, nextFactory);
	}

	public DefaultLocalVariableResolverFactory(final Map<String, Object> variables,
			final boolean cachingSafe) {
		super(variables);
	}

	public DefaultLocalVariableResolverFactory(final VariableResolverFactory nextFactory) {
		super(new HashMap<String, Object>(), nextFactory);
	}

	public DefaultLocalVariableResolverFactory(final VariableResolverFactory nextFactory,
			final String[] indexedVariables) {
		super(new HashMap<String, Object>(), nextFactory);
		this.indexedVariableNames = indexedVariables;
		this.indexedVariableResolvers = new VariableResolver[indexedVariables.length];
	}

	@Override
	public VariableResolver getIndexedVariableResolver(final int index) {
		if (indexedVariableNames == null) {
			return null;
		}

		if (indexedVariableResolvers[index] == null) {
			/**
			 * If the register is null, this means we need to forward-allocate the
			 * variable onto the
			 * register table.
			 */
			return indexedVariableResolvers[index] = super.getVariableResolver(
					indexedVariableNames[index]);
		}
		return indexedVariableResolvers[index];
	}

	@Override
	public VariableResolver getVariableResolver(final String name) {
		if (indexedVariableNames == null) {
			return super.getVariableResolver(name);
		}

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
	public VariableResolver createVariable(final String name, final Object value,
			final Class<?> type) {
		if (indexedVariableNames == null) {
			return super.createVariable(name, value, type);
		}

		VariableResolver vr;
		boolean newVar = false;

		try {
			int idx;
			if ((idx = variableIndexOf(name)) != -1) {
				vr = new SimpleValueResolver(value);
				if (indexedVariableResolvers[idx] == null) {
					indexedVariableResolvers[idx] = vr;
				}
				variableResolvers.put(indexedVariableNames[idx], vr);
				vr = indexedVariableResolvers[idx];

				newVar = true;
			} else {
				return super.createVariable(name, value, type);
			}

		} catch (final UnresolveablePropertyException e) {
			vr = null;
		}

		if (!newVar && vr != null && vr.getType() != null) {
			throw new RuntimeException(
					"variable already defined within scope: " + vr.getType() + " " + name);
		} else {
			addResolver(name, vr = new MapVariableResolver(variables, name, type)).setValue(value);
			return vr;
		}
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

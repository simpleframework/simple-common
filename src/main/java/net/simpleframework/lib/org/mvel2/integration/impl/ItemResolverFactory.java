/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.simpleframework.lib.org.mvel2.integration.impl;

import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class ItemResolverFactory extends BaseVariableResolverFactory {
	private final ItemResolver resolver;

	public ItemResolverFactory(final ItemResolver resolver, final VariableResolverFactory nextFactory) {
		this.resolver = resolver;
		this.nextFactory = nextFactory;
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value) {
		if (isTarget(name)) {
			resolver.setValue(value);
			return resolver;
		} else {
			return nextFactory.createVariable(name, value);
		}
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value, final Class<?> type) {
		if (isTarget(name)) {
			throw new RuntimeException("variable already defined in scope: " + name);
		} else {
			return nextFactory.createVariable(name, value);
		}
	}

	@Override
	public VariableResolver getVariableResolver(final String name) {
		return isTarget(name) ? resolver : nextFactory.getVariableResolver(name);
	}

	@Override
	public boolean isTarget(final String name) {
		return resolver.getName().equals(name);
	}

	@Override
	public boolean isResolveable(final String name) {
		return resolver.getName().equals(name)
				|| (nextFactory != null && nextFactory.isResolveable(name));
	}

	public static class ItemResolver implements VariableResolver {
		private final String name;
		private Class type = Object.class;
		public Object value;

		public ItemResolver(final String name, final Class type) {
			this.name = name;
			this.type = type;
		}

		public ItemResolver(final String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class getType() {
			return type;
		}

		@Override
		public void setStaticType(final Class type) {
			this.type = type;
		}

		@Override
		public int getFlags() {
			return 0;
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public void setValue(final Object value) {
			this.value = value;
		}
	}
}

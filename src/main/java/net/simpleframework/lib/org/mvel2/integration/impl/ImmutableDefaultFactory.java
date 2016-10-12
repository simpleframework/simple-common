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

import java.util.Set;

import net.simpleframework.lib.org.mvel2.ScriptRuntimeException;
import net.simpleframework.lib.org.mvel2.UnresolveablePropertyException;
import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class ImmutableDefaultFactory implements VariableResolverFactory {
	private boolean tiltFlag;

	private void throwError() {
		throw new ScriptRuntimeException(
				"cannot assign variables; no variable resolver factory available.");
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value) {
		throwError();
		return null;
	}

	@Override
	public VariableResolver createIndexedVariable(final int index, final String name,
			final Object value) {
		throwError();
		return null;
	}

	@Override
	public VariableResolver createVariable(final String name, final Object value,
			final Class<?> type) {
		throwError();
		return null;
	}

	@Override
	public VariableResolver createIndexedVariable(final int index, final String name,
			final Object value, final Class<?> typee) {
		throwError();
		return null;
	}

	@Override
	public VariableResolver setIndexedVariableResolver(final int index,
			final VariableResolver variableResolver) {
		throwError();
		return null;
	}

	@Override
	public VariableResolverFactory getNextFactory() {
		return null;
	}

	@Override
	public VariableResolverFactory setNextFactory(final VariableResolverFactory resolverFactory) {
		throw new RuntimeException("cannot chain to this factory");
	}

	@Override
	public VariableResolver getVariableResolver(final String name) {
		throw new UnresolveablePropertyException(name);
	}

	@Override
	public VariableResolver getIndexedVariableResolver(final int index) {
		throwError();
		return null;
	}

	@Override
	public boolean isTarget(final String name) {
		return false;
	}

	@Override
	public boolean isResolveable(final String name) {
		return false;
	}

	@Override
	public Set<String> getKnownVariables() {
		return null;
	}

	@Override
	public int variableIndexOf(final String name) {
		return -1;
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
	public void setTiltFlag(final boolean tilt) {
		this.tiltFlag = tilt;
	}
}

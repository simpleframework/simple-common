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

package net.simpleframework.lib.org.mvel2.compiler;

import net.simpleframework.lib.org.mvel2.ast.Safe;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Christopher Brock
 */
public class ExecutableLiteral implements ExecutableStatement, Safe {
	private Object literal;
	private int integer32;
	private boolean intOptimized;

	public ExecutableLiteral(final Object literal) {
		if ((this.literal = literal) instanceof Integer) {
			this.integer32 = (Integer) literal;
		}
	}

	public ExecutableLiteral(final int literal) {
		this.literal = this.integer32 = literal;
		this.intOptimized = true;
	}

	public int getInteger32() {
		return integer32;
	}

	public void setInteger32(final int integer32) {
		this.integer32 = integer32;
	}

	@Override
	public Object getValue(final Object staticContext, final VariableResolverFactory factory) {
		return literal;
	}

	@Override
	public void setKnownIngressType(final Class type) {

	}

	@Override
	public void setKnownEgressType(final Class type) {

	}

	@Override
	public Class getKnownIngressType() {
		return null;
	}

	@Override
	public Class getKnownEgressType() {
		return this.literal == null ? Object.class : this.literal.getClass();
	}

	@Override
	public boolean isConvertableIngressEgress() {
		return false;
	}

	@Override
	public void computeTypeConversionRule() {

	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) {
		return literal;
	}

	public Object getLiteral() {
		return literal;
	}

	@Override
	public boolean intOptimized() {
		return intOptimized;
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		// not implemented
		return null;
	}

	@Override
	public boolean isLiteralOnly() {
		return true;
	}

	@Override
	public boolean isEmptyStatement() {
		return false;
	}

	@Override
	public boolean isExplicitCast() {
		return false;
	}
}

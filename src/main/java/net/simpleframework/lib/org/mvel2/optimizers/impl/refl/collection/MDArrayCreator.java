/**
 * MVEL (The MVFLEX Expression Language)
 *
 * Copyright (C) 2007 Christopher Brock, MVFLEX/Valhalla Project and the
 * Codehaus
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
 *
 */
package net.simpleframework.lib.org.mvel2.optimizers.impl.refl.collection;

import static java.lang.reflect.Array.newInstance;

import java.lang.reflect.Array;

import net.simpleframework.lib.org.mvel2.compiler.Accessor;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Christopher Brock
 */
public class MDArrayCreator implements Accessor {
	public Accessor[] template;
	private final Class arrayType;
	private final int dimension;

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) {
		if (Object.class.equals(arrayType)) {
			final Object[] newArray = new Object[template.length];

			for (int i = 0; i < newArray.length; i++) {
				newArray[i] = template[i].getValue(ctx, elCtx, variableFactory);
			}

			return newArray;
		} else {
			final Object newArray = newInstance(arrayType, template.length);

			for (int i = 0; i < template.length; i++) {
				final Object o = template[i].getValue(ctx, elCtx, variableFactory);
				Array.set(newArray, i, o);
			}

			return newArray;
		}
	}

	public MDArrayCreator(final Accessor[] template, final Class arrayType, final int dimension) {
		this.template = template;
		this.arrayType = arrayType;
		this.dimension = dimension;
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		return null;
	}

	@Override
	public Class getKnownEgressType() {
		return arrayType;
	}
}
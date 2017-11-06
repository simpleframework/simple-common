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

package net.simpleframework.lib.org.mvel2.conversion;

import java.util.HashMap;
import java.util.Map;

import net.simpleframework.lib.org.mvel2.ConversionHandler;

public class StringArrayCH implements ConversionHandler {
	private static final Map<Class, Converter> CNV = new HashMap<>();

	@Override
	public Object convertFrom(final Object in) {

		if (in.getClass().isArray()) {

			final Object[] old = (Object[]) in;
			final String[] n = new String[old.length];
			for (int i = 0; i < old.length; i++) {
				n[i] = String.valueOf(old[i]);
			}

			return n;
		} else {
			return new String[] { String.valueOf(in) };
		}

		// return CNV.get(in.getClass()).convert(in);
	}

	@Override
	public boolean canConvertFrom(final Class cls) {
		return CNV.containsKey(cls);
	}

	static {
		CNV.put(Object[].class, new Converter() {
			@Override
			public Object convert(final Object o) {
				final Object[] old = (Object[]) o;
				final String[] n = new String[old.length];
				for (int i = 0; i < old.length; i++) {
					n[i] = String.valueOf(old[i]);
				}

				return n;
			}
		});

	}
}

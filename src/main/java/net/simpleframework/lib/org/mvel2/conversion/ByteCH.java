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

package net.simpleframework.lib.org.mvel2.conversion;

import static java.lang.String.valueOf;

import java.util.HashMap;
import java.util.Map;

import net.simpleframework.lib.org.mvel2.ConversionException;
import net.simpleframework.lib.org.mvel2.ConversionHandler;

public class ByteCH implements ConversionHandler {
	private static final Map<Class, Converter> CNV = new HashMap<Class, Converter>();

	private static Converter stringConverter = new Converter() {
		@Override
		public Object convert(final Object o) {
			return Byte.parseByte(((String) o));
		}
	};

	@Override
	public Object convertFrom(final Object in) {
		if (!CNV.containsKey(in.getClass())) {
			throw new ConversionException("cannot convert type: " + in.getClass().getName() + " to: "
					+ Integer.class.getName());
		}
		return CNV.get(in.getClass()).convert(in);
	}

	@Override
	public boolean canConvertFrom(final Class cls) {
		return CNV.containsKey(cls);
	}

	static {
		CNV.put(String.class, stringConverter);

		CNV.put(Object.class, new Converter() {
			@Override
			public Object convert(final Object o) {
				return stringConverter.convert(valueOf(o));
			}
		});

		CNV.put(Byte.class, new Converter() {
			@Override
			public Object convert(final Object o) {
				// noinspection UnnecessaryBoxing
				return new Byte(((Byte) o));
			}
		});

		CNV.put(Integer.class, new Converter() {
			@Override
			public Object convert(final Object o) {
				return ((Integer) o).byteValue();
			}
		});

		CNV.put(Long.class, new Converter() {
			@Override
			public Object convert(final Object o) {
				return ((Long) o).byteValue();
			}
		});

		CNV.put(Double.class, new Converter() {
			@Override
			public Object convert(final Object o) {
				return ((Double) o).byteValue();
			}
		});

		CNV.put(Float.class, new Converter() {
			@Override
			public Object convert(final Object o) {
				return ((Float) o).byteValue();
			}
		});

		CNV.put(Short.class, new Converter() {
			@Override
			public Object convert(final Object o) {
				return ((Short) o).byteValue();
			}
		});
	}
}

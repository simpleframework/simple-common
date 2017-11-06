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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import net.simpleframework.lib.org.mvel2.ConversionException;
import net.simpleframework.lib.org.mvel2.ConversionHandler;

public class BigIntegerCH implements ConversionHandler {
	private static final Map<Class, Converter> CNV = new HashMap<>();

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
		CNV.put(Object.class, new Converter() {
			@Override
			public BigInteger convert(final Object o) {
				return new BigInteger(String.valueOf(o));
			}
		});

		CNV.put(BigInteger.class, new Converter() {
			@Override
			public BigInteger convert(final Object o) {
				return (BigInteger) o;
			}
		});

		CNV.put(BigDecimal.class, new Converter() {
			@Override
			public BigInteger convert(final Object o) {
				return ((BigDecimal) o).toBigInteger();
			}
		});

		CNV.put(String.class, new Converter() {
			@Override
			public BigInteger convert(final Object o) {
				return new BigInteger((String) o);
			}
		});

		CNV.put(Short.class, new Converter() {
			@Override
			public BigInteger convert(final Object o) {
				return new BigInteger(String.valueOf(o));
			}
		});

		CNV.put(Long.class, new Converter() {
			@Override
			public BigInteger convert(final Object o) {
				return new BigInteger(String.valueOf(o));
			}
		});

		CNV.put(Integer.class, new Converter() {
			@Override
			public BigInteger convert(final Object o) {
				return new BigInteger(String.valueOf(o));
			}
		});

		CNV.put(String.class, new Converter() {
			@Override
			public BigInteger convert(final Object o) {
				return new BigInteger((String) o);
			}
		});

		CNV.put(char[].class, new Converter() {
			@Override
			public BigInteger convert(final Object o) {
				return new BigInteger(new String((char[]) o));
			}
		}

		);
	}
}

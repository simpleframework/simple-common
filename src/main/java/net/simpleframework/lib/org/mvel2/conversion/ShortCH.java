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

import static java.lang.Short.parseShort;
import static java.lang.String.valueOf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import net.simpleframework.lib.org.mvel2.ConversionException;
import net.simpleframework.lib.org.mvel2.ConversionHandler;

public class ShortCH implements ConversionHandler {
	/**
	 * This is purely because Eclipse sucks, and has a serious bug with
	 * it's java parser.
	 */
	private static final Short TRUE = (short) 1;
	private static final Short FALSE = (short) 0;

	private static Converter stringConverter = new Converter() {
		@Override
		public Short convert(final Object o) {
			return parseShort(((String) o));
		}
	};

	private static final Map<Class, Converter> CNV = new HashMap<>();

	@Override
	public Object convertFrom(final Object in) {
		if (!CNV.containsKey(in.getClass())) {
			throw new ConversionException(
					"cannot convert type: " + in.getClass().getName() + " to: " + Short.class.getName());
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

		CNV.put(BigDecimal.class, new Converter() {
			@Override
			public Short convert(final Object o) {
				return ((BigDecimal) o).shortValue();
			}
		});

		CNV.put(BigInteger.class, new Converter() {
			@Override
			public Short convert(final Object o) {
				return ((BigInteger) o).shortValue();
			}
		});

		CNV.put(Short.class, new Converter() {
			@Override
			public Object convert(final Object o) {
				return o;
			}
		});

		CNV.put(Integer.class, new Converter() {
			@Override
			public Short convert(final Object o) {
				if (((Integer) o) > Short.MAX_VALUE) {
					throw new ConversionException("cannot coerce Integer to Short since the value ("
							+ valueOf(o) + ") exceeds that maximum precision of Integer.");
				} else {
					return ((Integer) o).shortValue();
				}
			}
		});

		CNV.put(Float.class, new Converter() {
			@Override
			public Short convert(final Object o) {
				if (((Float) o) > Short.MAX_VALUE) {
					throw new ConversionException("cannot coerce Float to Short since the value ("
							+ valueOf(o) + ") exceeds that maximum precision of Integer.");
				} else {
					return ((Float) o).shortValue();
				}
			}
		});

		CNV.put(Double.class, new Converter() {
			@Override
			public Short convert(final Object o) {
				if (((Double) o) > Short.MAX_VALUE) {
					throw new ConversionException("cannot coerce Double to Short since the value ("
							+ valueOf(o) + ") exceeds that maximum precision of Integer.");
				} else {
					return ((Double) o).shortValue();
				}
			}
		});

		CNV.put(Long.class, new Converter() {
			@Override
			public Short convert(final Object o) {
				if (((Long) o) > Short.MAX_VALUE) {
					throw new ConversionException("cannot coerce Integer to Short since the value ("
							+ valueOf(o) + ") exceeds that maximum precision of Integer.");
				} else {
					return ((Long) o).shortValue();
				}
			}
		});

		CNV.put(Boolean.class, new Converter() {
			@Override
			public Short convert(final Object o) {

				if ((Boolean) o) {
					return TRUE;
				} else {
					return FALSE;
				}

			}
		});

	}
}

package net.simpleframework.lib.net.minidev.json.writer;

/*
 * Copyright 2011 JSON-SMART authors
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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArraysMapper<T> extends JsonReaderI<T> {
	public ArraysMapper(final JsonReader base) {
		super(base);
	}

	@Override
	public Object createArray() {
		return new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addValue(final Object current, final Object value) {
		((List<Object>) current).add(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T convert(final Object current) {
		return (T) current;
	}

	public static class GenericMapper<T> extends ArraysMapper<T> {
		final Class<?> componentType;
		JsonReaderI<?> subMapper;

		public GenericMapper(final JsonReader base, final Class<T> type) {
			super(base);
			this.componentType = type.getComponentType();
		}

		@SuppressWarnings("unchecked")
		@Override
		public T convert(final Object current) {
			int p = 0;

			final Object[] r = (Object[]) Array.newInstance(componentType, ((List<?>) current).size());
			for (final Object e : ((List<?>) current)) {
				r[p++] = e;
			}
			return (T) r;
		}

		@Override
		public JsonReaderI<?> startArray(final String key) {
			if (subMapper == null) {
				subMapper = base.getMapper(componentType);
			}
			return subMapper;
		}

		@Override
		public JsonReaderI<?> startObject(final String key) {
			if (subMapper == null) {
				subMapper = base.getMapper(componentType);
			}
			return subMapper;
		}
	};

	public static JsonReaderI<int[]> MAPPER_PRIM_INT = new ArraysMapper<int[]>(null) {
		@Override
		public int[] convert(final Object current) {
			int p = 0;
			final int[] r = new int[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				r[p++] = ((Number) e).intValue();
			}
			return r;
		}
	};

	public static JsonReaderI<Integer[]> MAPPER_INT = new ArraysMapper<Integer[]>(null) {
		@Override
		public Integer[] convert(final Object current) {
			int p = 0;
			final Integer[] r = new Integer[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				if (e == null) {
					continue;
				}
				if (e instanceof Integer) {
					r[p] = (Integer) e;
				} else {
					r[p] = ((Number) e).intValue();
				}
				p++;
			}
			return r;
		}
	};

	public static JsonReaderI<short[]> MAPPER_PRIM_SHORT = new ArraysMapper<short[]>(null) {
		@Override
		public short[] convert(final Object current) {
			int p = 0;
			final short[] r = new short[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				r[p++] = ((Number) e).shortValue();
			}
			return r;
		}
	};

	public static JsonReaderI<Short[]> MAPPER_SHORT = new ArraysMapper<Short[]>(null) {
		@Override
		public Short[] convert(final Object current) {
			int p = 0;
			final Short[] r = new Short[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				if (e == null) {
					continue;
				}
				if (e instanceof Short) {
					r[p] = (Short) e;
				} else {
					r[p] = ((Number) e).shortValue();
				}
				p++;
			}
			return r;
		}
	};

	public static JsonReaderI<byte[]> MAPPER_PRIM_BYTE = new ArraysMapper<byte[]>(null) {
		@Override
		public byte[] convert(final Object current) {
			int p = 0;
			final byte[] r = new byte[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				r[p++] = ((Number) e).byteValue();
			}
			return r;
		}
	};

	public static JsonReaderI<Byte[]> MAPPER_BYTE = new ArraysMapper<Byte[]>(null) {
		@Override
		public Byte[] convert(final Object current) {
			int p = 0;
			final Byte[] r = new Byte[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				if (e == null) {
					continue;
				}
				if (e instanceof Byte) {
					r[p] = (Byte) e;
				} else {
					r[p] = ((Number) e).byteValue();
				}
				p++;
			}
			return r;
		}
	};

	public static JsonReaderI<char[]> MAPPER_PRIM_CHAR = new ArraysMapper<char[]>(null) {
		@Override
		public char[] convert(final Object current) {
			int p = 0;
			final char[] r = new char[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				r[p++] = e.toString().charAt(0);
			}
			return r;
		}
	};

	public static JsonReaderI<Character[]> MAPPER_CHAR = new ArraysMapper<Character[]>(null) {
		@Override
		public Character[] convert(final Object current) {
			int p = 0;
			final Character[] r = new Character[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				if (e == null) {
					continue;
				}
				r[p] = e.toString().charAt(0);
				p++;
			}
			return r;
		}
	};

	public static JsonReaderI<long[]> MAPPER_PRIM_LONG = new ArraysMapper<long[]>(null) {
		@Override
		public long[] convert(final Object current) {
			int p = 0;
			final long[] r = new long[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				r[p++] = ((Number) e).intValue();
			}
			return r;
		}
	};

	public static JsonReaderI<Long[]> MAPPER_LONG = new ArraysMapper<Long[]>(null) {
		@Override
		public Long[] convert(final Object current) {
			int p = 0;
			final Long[] r = new Long[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				if (e == null) {
					continue;
				}
				if (e instanceof Float) {
					r[p] = ((Long) e);
				} else {
					r[p] = ((Number) e).longValue();
				}
				p++;
			}
			return r;
		}
	};

	public static JsonReaderI<float[]> MAPPER_PRIM_FLOAT = new ArraysMapper<float[]>(null) {
		@Override
		public float[] convert(final Object current) {
			int p = 0;
			final float[] r = new float[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				r[p++] = ((Number) e).floatValue();
			}
			return r;
		}
	};

	public static JsonReaderI<Float[]> MAPPER_FLOAT = new ArraysMapper<Float[]>(null) {
		@Override
		public Float[] convert(final Object current) {
			int p = 0;
			final Float[] r = new Float[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				if (e == null) {
					continue;
				}
				if (e instanceof Float) {
					r[p] = ((Float) e);
				} else {
					r[p] = ((Number) e).floatValue();
				}
				p++;
			}
			return r;
		}
	};

	public static JsonReaderI<double[]> MAPPER_PRIM_DOUBLE = new ArraysMapper<double[]>(null) {
		@Override
		public double[] convert(final Object current) {
			int p = 0;
			final double[] r = new double[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				r[p++] = ((Number) e).doubleValue();
			}
			return r;
		}
	};

	public static JsonReaderI<Double[]> MAPPER_DOUBLE = new ArraysMapper<Double[]>(null) {
		@Override
		public Double[] convert(final Object current) {
			int p = 0;
			final Double[] r = new Double[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				if (e == null) {
					continue;
				}
				if (e instanceof Double) {
					r[p] = ((Double) e);
				} else {
					r[p] = ((Number) e).doubleValue();
				}
				p++;
			}
			return r;
		}
	};

	public static JsonReaderI<boolean[]> MAPPER_PRIM_BOOL = new ArraysMapper<boolean[]>(null) {
		@Override
		public boolean[] convert(final Object current) {
			int p = 0;
			final boolean[] r = new boolean[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				r[p++] = ((Boolean) e).booleanValue();
			}
			return r;
		}
	};

	public static JsonReaderI<Boolean[]> MAPPER_BOOL = new ArraysMapper<Boolean[]>(null) {
		@Override
		public Boolean[] convert(final Object current) {
			int p = 0;
			final Boolean[] r = new Boolean[((List<?>) current).size()];
			for (final Object e : ((List<?>) current)) {
				if (e == null) {
					continue;
				}
				if (e instanceof Boolean) {
					r[p] = ((Boolean) e).booleanValue();
				} else if (e instanceof Number) {
					r[p] = ((Number) e).intValue() != 0;
				} else {
					throw new RuntimeException("can not convert " + e + " toBoolean");
				}
				p++;
			}
			return r;
		}
	};
}

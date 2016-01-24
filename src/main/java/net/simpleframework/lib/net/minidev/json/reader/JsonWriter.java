package net.simpleframework.lib.net.minidev.json.reader;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.simpleframework.lib.net.minidev.json.JSONAware;
import net.simpleframework.lib.net.minidev.json.JSONAwareEx;
import net.simpleframework.lib.net.minidev.json.JSONStreamAware;
import net.simpleframework.lib.net.minidev.json.JSONStreamAwareEx;
import net.simpleframework.lib.net.minidev.json.JSONStyle;
import net.simpleframework.lib.net.minidev.json.JSONValue;

public class JsonWriter {
	private final ConcurrentHashMap<Class<?>, JsonWriterI<?>> data;
	private final LinkedList<WriterByInterface> writerInterfaces;

	public JsonWriter() {
		data = new ConcurrentHashMap<Class<?>, JsonWriterI<?>>();
		writerInterfaces = new LinkedList<WriterByInterface>();
		init();
	}

	/**
	 * remap field name in custom classes
	 * 
	 * @param fromJava
	 *        field name in java
	 * @param toJson
	 *        field name in json
	 * @since 2.1.1
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> void remapField(final Class<T> type, final String fromJava, final String toJson) {
		JsonWriterI map = this.getWrite(type);
		if (!(map instanceof BeansWriterASMRemap)) {
			map = new BeansWriterASMRemap();
			registerWriter(map, type);
		}
		((BeansWriterASMRemap) map).renameField(fromJava, toJson);
	}

	static class WriterByInterface {
		public Class<?> _interface;
		public JsonWriterI<?> _writer;

		public WriterByInterface(final Class<?> _interface, final JsonWriterI<?> _writer) {
			this._interface = _interface;
			this._writer = _writer;
		}
	}

	/**
	 * try to find a Writer by Cheking implemented interface
	 * 
	 * @param clazz
	 *        class to serialize
	 * @return a Writer or null
	 */
	@SuppressWarnings("rawtypes")
	public JsonWriterI getWriterByInterface(final Class<?> clazz) {
		for (final WriterByInterface w : writerInterfaces) {
			if (w._interface.isAssignableFrom(clazz)) {
				return w._writer;
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public JsonWriterI getWrite(final Class cls) {
		return data.get(cls);
	}

	final static public JsonWriterI<JSONStreamAwareEx> JSONStreamAwareWriter = new JsonWriterI<JSONStreamAwareEx>() {
		@Override
		public <E extends JSONStreamAwareEx> void writeJSONString(final E value,
				final Appendable out, final JSONStyle compression) throws IOException {
			value.writeJSONString(out);
		}
	};

	final static public JsonWriterI<JSONStreamAwareEx> JSONStreamAwareExWriter = new JsonWriterI<JSONStreamAwareEx>() {
		@Override
		public <E extends JSONStreamAwareEx> void writeJSONString(final E value,
				final Appendable out, final JSONStyle compression) throws IOException {
			value.writeJSONString(out, compression);
		}
	};

	final static public JsonWriterI<JSONAwareEx> JSONJSONAwareExWriter = new JsonWriterI<JSONAwareEx>() {
		@Override
		public <E extends JSONAwareEx> void writeJSONString(final E value, final Appendable out,
				final JSONStyle compression) throws IOException {
			out.append(value.toJSONString(compression));
		}
	};

	final static public JsonWriterI<JSONAware> JSONJSONAwareWriter = new JsonWriterI<JSONAware>() {
		@Override
		public <E extends JSONAware> void writeJSONString(final E value, final Appendable out,
				final JSONStyle compression) throws IOException {
			out.append(value.toJSONString());
		}
	};

	final static public JsonWriterI<Iterable<? extends Object>> JSONIterableWriter = new JsonWriterI<Iterable<? extends Object>>() {
		@Override
		public <E extends Iterable<? extends Object>> void writeJSONString(final E list,
				final Appendable out, final JSONStyle compression) throws IOException {
			boolean first = true;
			compression.arrayStart(out);
			for (final Object value : list) {
				if (first) {
					first = false;
					compression.arrayfirstObject(out);
				} else {
					compression.arrayNextElm(out);
				}
				if (value == null) {
					out.append("null");
				} else {
					JSONValue.writeJSONString(value, out, compression);
				}
				compression.arrayObjectEnd(out);
			}
			compression.arrayStop(out);
		}
	};

	final static public JsonWriterI<Enum<?>> EnumWriter = new JsonWriterI<Enum<?>>() {
		@Override
		public <E extends Enum<?>> void writeJSONString(final E value, final Appendable out,
				final JSONStyle compression) throws IOException {
			@SuppressWarnings("rawtypes")
			final String s = ((Enum) value).name();
			compression.writeString(out, s);
		}
	};

	final static public JsonWriterI<Map<String, ? extends Object>> JSONMapWriter = new JsonWriterI<Map<String, ? extends Object>>() {
		@Override
		public <E extends Map<String, ? extends Object>> void writeJSONString(final E map,
				final Appendable out, final JSONStyle compression) throws IOException {
			boolean first = true;
			compression.objectStart(out);
			/**
			 * do not use <String, Object> to handle non String key maps
			 */
			for (final Map.Entry<?, ?> entry : map.entrySet()) {
				final Object v = entry.getValue();
				if (v == null && compression.ignoreNull()) {
					continue;
				}
				if (first) {
					compression.objectFirstStart(out);
					first = false;
				} else {
					compression.objectNext(out);
				}
				JsonWriter.writeJSONKV(entry.getKey().toString(), v, out, compression);
				// compression.objectElmStop(out);
			}
			compression.objectStop(out);
		}
	};

	/**
	 * Json-Smart V2 Beans serialiser
	 * 
	 * Based on ASM
	 */
	final static public JsonWriterI<Object> beansWriterASM = new BeansWriterASM();

	/**
	 * Json-Smart V1 Beans serialiser
	 */
	final static public JsonWriterI<Object> beansWriter = new BeansWriter();

	/**
	 * Json-Smart ArrayWriterClass
	 */
	final static public JsonWriterI<Object> arrayWriter = new ArrayWriter();

	/**
	 * ToString Writer
	 */
	final static public JsonWriterI<Object> toStringWriter = new JsonWriterI<Object>() {
		@Override
		public void writeJSONString(final Object value, final Appendable out,
				final JSONStyle compression) throws IOException {
			out.append(value.toString());
		}
	};

	public void init() {
		registerWriter(new JsonWriterI<String>() {
			@Override
			public void writeJSONString(final String value, final Appendable out,
					final JSONStyle compression) throws IOException {
				compression.writeString(out, value);
			}
		}, String.class);

		registerWriter(new JsonWriterI<Double>() {
			@Override
			public void writeJSONString(final Double value, final Appendable out,
					final JSONStyle compression) throws IOException {
				if (value.isInfinite()) {
					out.append("null");
				} else {
					out.append(value.toString());
				}
			}
		}, Double.class);

		registerWriter(new JsonWriterI<Date>() {
			@Override
			public void writeJSONString(final Date value, final Appendable out,
					final JSONStyle compression) throws IOException {
				out.append('"');
				JSONValue.escape(value.toString(), out, compression);
				out.append('"');
			}
		}, Date.class);

		registerWriter(new JsonWriterI<Float>() {
			@Override
			public void writeJSONString(final Float value, final Appendable out,
					final JSONStyle compression) throws IOException {
				if (value.isInfinite()) {
					out.append("null");
				} else {
					out.append(value.toString());
				}
			}
		}, Float.class);

		registerWriter(toStringWriter, Integer.class, Long.class, Byte.class, Short.class,
				BigInteger.class, BigDecimal.class);
		registerWriter(toStringWriter, Boolean.class);

		/**
		 * Array
		 */

		registerWriter(new JsonWriterI<int[]>() {
			@Override
			public void writeJSONString(final int[] value, final Appendable out,
					final JSONStyle compression) throws IOException {
				boolean needSep = false;
				compression.arrayStart(out);
				for (final int b : value) {
					if (needSep) {
						compression.objectNext(out);
					} else {
						needSep = true;
					}
					out.append(Integer.toString(b));
				}
				compression.arrayStop(out);
			}
		}, int[].class);

		registerWriter(new JsonWriterI<short[]>() {
			@Override
			public void writeJSONString(final short[] value, final Appendable out,
					final JSONStyle compression) throws IOException {
				boolean needSep = false;
				compression.arrayStart(out);
				for (final short b : value) {
					if (needSep) {
						compression.objectNext(out);
					} else {
						needSep = true;
					}
					out.append(Short.toString(b));
				}
				compression.arrayStop(out);
			}
		}, short[].class);

		registerWriter(new JsonWriterI<long[]>() {
			@Override
			public void writeJSONString(final long[] value, final Appendable out,
					final JSONStyle compression) throws IOException {
				boolean needSep = false;
				compression.arrayStart(out);
				for (final long b : value) {
					if (needSep) {
						compression.objectNext(out);
					} else {
						needSep = true;
					}
					out.append(Long.toString(b));
				}
				compression.arrayStop(out);
			}
		}, long[].class);

		registerWriter(new JsonWriterI<float[]>() {
			@Override
			public void writeJSONString(final float[] value, final Appendable out,
					final JSONStyle compression) throws IOException {
				boolean needSep = false;
				compression.arrayStart(out);
				for (final float b : value) {
					if (needSep) {
						compression.objectNext(out);
					} else {
						needSep = true;
					}
					out.append(Float.toString(b));
				}
				compression.arrayStop(out);
			}
		}, float[].class);

		registerWriter(new JsonWriterI<double[]>() {
			@Override
			public void writeJSONString(final double[] value, final Appendable out,
					final JSONStyle compression) throws IOException {
				boolean needSep = false;
				compression.arrayStart(out);
				for (final double b : value) {
					if (needSep) {
						compression.objectNext(out);
					} else {
						needSep = true;
					}
					out.append(Double.toString(b));
				}
				compression.arrayStop(out);
			}
		}, double[].class);

		registerWriter(new JsonWriterI<boolean[]>() {
			@Override
			public void writeJSONString(final boolean[] value, final Appendable out,
					final JSONStyle compression) throws IOException {
				boolean needSep = false;
				compression.arrayStart(out);
				for (final boolean b : value) {
					if (needSep) {
						compression.objectNext(out);
					} else {
						needSep = true;
					}
					out.append(Boolean.toString(b));
				}
				compression.arrayStop(out);
			}
		}, boolean[].class);

		registerWriterInterface(JSONStreamAwareEx.class, JsonWriter.JSONStreamAwareExWriter);
		registerWriterInterface(JSONStreamAware.class, JsonWriter.JSONStreamAwareWriter);
		registerWriterInterface(JSONAwareEx.class, JsonWriter.JSONJSONAwareExWriter);
		registerWriterInterface(JSONAware.class, JsonWriter.JSONJSONAwareWriter);
		registerWriterInterface(Map.class, JsonWriter.JSONMapWriter);
		registerWriterInterface(Iterable.class, JsonWriter.JSONIterableWriter);
		registerWriterInterface(Enum.class, JsonWriter.EnumWriter);
		registerWriterInterface(Number.class, JsonWriter.toStringWriter);
	}

	/**
	 * associate an Writer to a interface With Hi priority
	 * 
	 * @param interFace
	 *        interface to map
	 * @param writer
	 *        writer Object
	 * @deprecated use registerWriterInterfaceFirst
	 */
	@Deprecated
	public void addInterfaceWriterFirst(final Class<?> interFace, final JsonWriterI<?> writer) {
		registerWriterInterfaceFirst(interFace, writer);
	}

	/**
	 * associate an Writer to a interface With Low priority
	 * 
	 * @param interFace
	 *        interface to map
	 * @param writer
	 *        writer Object
	 * @deprecated use registerWriterInterfaceLast
	 */
	@Deprecated
	public void addInterfaceWriterLast(final Class<?> interFace, final JsonWriterI<?> writer) {
		registerWriterInterfaceLast(interFace, writer);
	}

	/**
	 * associate an Writer to a interface With Low priority
	 * 
	 * @param interFace
	 *        interface to map
	 * @param writer
	 *        writer Object
	 */
	public void registerWriterInterfaceLast(final Class<?> interFace, final JsonWriterI<?> writer) {
		writerInterfaces.addLast(new WriterByInterface(interFace, writer));
	}

	/**
	 * associate an Writer to a interface With Hi priority
	 * 
	 * @param interFace
	 *        interface to map
	 * @param writer
	 *        writer Object
	 */
	public void registerWriterInterfaceFirst(final Class<?> interFace, final JsonWriterI<?> writer) {
		writerInterfaces.addFirst(new WriterByInterface(interFace, writer));
	}

	/**
	 * an alias for registerWriterInterfaceLast
	 * 
	 * @param interFace
	 *        interface to map
	 * @param writer
	 *        writer Object
	 */
	public void registerWriterInterface(final Class<?> interFace, final JsonWriterI<?> writer) {
		registerWriterInterfaceLast(interFace, writer);
	}

	/**
	 * associate an Writer to a Class
	 * 
	 * @param writer
	 * @param cls
	 */
	public <T> void registerWriter(final JsonWriterI<T> writer, final Class<?>... cls) {
		for (final Class<?> c : cls) {
			data.put(c, writer);
		}
	}

	/**
	 * Write a Key : value entry to a stream
	 */
	public static void writeJSONKV(final String key, final Object value, final Appendable out,
			final JSONStyle compression) throws IOException {
		if (key == null) {
			out.append("null");
		} else if (!compression.mustProtectKey(key)) {
			out.append(key);
		} else {
			out.append('"');
			JSONValue.escape(key, out, compression);
			out.append('"');
		}
		compression.objectEndOfKey(out);
		if (value instanceof String) {
			compression.writeString(out, (String) value);
		} else {
			JSONValue.writeJSONString(value, out, compression);
		}
		compression.objectElmStop(out);
	}
}

package net.simpleframework.lib.net.minidev.json;

/*
 *    Copyright 2011 JSON-SMART authors
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
import static net.simpleframework.lib.net.minidev.json.parser.JSONParser.DEFAULT_PERMISSIVE_MODE;
import static net.simpleframework.lib.net.minidev.json.parser.JSONParser.MODE_RFC4627;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.simpleframework.lib.net.minidev.asm.Accessor;
import net.simpleframework.lib.net.minidev.asm.BeansAccess;
import net.simpleframework.lib.net.minidev.json.mapper.AMapper;
import net.simpleframework.lib.net.minidev.json.mapper.CompessorMapper;
import net.simpleframework.lib.net.minidev.json.mapper.DefaultMapper;
import net.simpleframework.lib.net.minidev.json.mapper.DefaultMapperOrdered;
import net.simpleframework.lib.net.minidev.json.mapper.FakeMapper;
import net.simpleframework.lib.net.minidev.json.mapper.Mapper;
import net.simpleframework.lib.net.minidev.json.mapper.UpdaterMapper;
import net.simpleframework.lib.net.minidev.json.parser.JSONParser;
import net.simpleframework.lib.net.minidev.json.parser.ParseException;

/**
 * JSONValue is the helper class In most of case you should use those static
 * methode to user JSON-smart
 * 
 * 
 * The most commonly use methode are {@link #parse(String)}
 * {@link #toJSONString(Object)}
 * 
 * @author Uriel Chemouni <uchemouni@gmail.com>
 */
public class JSONValue {
	/**
	 * Global default compression type
	 */
	public static JSONStyle COMPRESSION = JSONStyle.NO_COMPRESS;

	/**
	 * Parse JSON text into java object from the input source. Please use
	 * parseWithException() if you don't want to ignore the exception. if you
	 * want strict input check use parseStrict()
	 * 
	 * @see JSONParser#parse(Reader)
	 * @see #parseWithException(Reader)
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 * 
	 */
	public static Object parse(final InputStream in) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Parse JSON text into java object from the input source. Please use
	 * parseWithException() if you don't want to ignore the exception. if you
	 * want strict input check use parseStrict()
	 * 
	 * @see JSONParser#parse(Reader)
	 * @see #parseWithException(Reader)
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 * 
	 */
	public static Object parse(final byte[] in) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Parse input json as a mapTo class
	 * 
	 * mapTo can be a bean
	 * 
	 * @since 2.0
	 */
	public static <T> T parse(final InputStream in, final Class<T> mapTo) {
		try {
			final JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
			return p.parse(in, Mapper.getMapper(mapTo));
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parse JSON text into java object from the input source. Please use
	 * parseWithException() if you don't want to ignore the exception. if you
	 * want strict input check use parseStrict()
	 * 
	 * @see JSONParser#parse(Reader)
	 * @see #parseWithException(Reader)
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 * 
	 */
	public static Object parse(final Reader in) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Parse input json as a mapTo class
	 * 
	 * mapTo can be a bean
	 * 
	 * @since 2.0
	 */
	public static <T> T parse(final byte[] in, final Class<T> mapTo) {
		try {
			final JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
			return p.parse(in, Mapper.getMapper(mapTo));
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parse input json as a mapTo class
	 * 
	 * mapTo can be a bean
	 * 
	 * @since 2.0
	 */
	public static <T> T parse(final Reader in, final Class<T> mapTo) {
		try {
			final JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
			return p.parse(in, Mapper.getMapper(mapTo));
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parse input json as a mapTo class
	 * 
	 * mapTo can be a bean
	 * 
	 * @since 2.0
	 */
	public static <T> T parse(final Reader in, final T toUpdate) {
		try {
			final JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
			return p.parse(in, new UpdaterMapper<T>(toUpdate));
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parse input json as a mapTo class
	 * 
	 * @since 2.0
	 */
	protected static <T> T parse(final Reader in, final AMapper<T> mapper) {
		try {
			final JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
			return p.parse(in, mapper);
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parse input json as a mapTo class
	 * 
	 * mapTo can be a bean
	 * 
	 * @since 2.0
	 */
	public static <T> T parse(final String in, final Class<T> mapTo) {
		try {
			final JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
			return p.parse(in, Mapper.getMapper(mapTo));
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parse input json as a mapTo class
	 * 
	 * mapTo can be a bean
	 * 
	 * @since 2.0
	 */
	public static <T> T parse(final InputStream in, final T toUpdate) {
		try {
			final JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
			return p.parse(in, new UpdaterMapper<T>(toUpdate));
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parse input json as a mapTo class
	 * 
	 * mapTo can be a bean
	 * 
	 * @since 2.0
	 */
	public static <T> T parse(final String in, final T toUpdate) {
		try {
			final JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
			return p.parse(in, new UpdaterMapper<T>(toUpdate));
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parse input json as a mapTo class
	 * 
	 * @since 2.0
	 */
	protected static <T> T parse(final byte[] in, final AMapper<T> mapper) {
		try {
			final JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
			return p.parse(in, mapper);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Parse input json as a mapTo class
	 * 
	 * @since 2.0
	 */
	protected static <T> T parse(final String in, final AMapper<T> mapper) {
		try {
			final JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
			return p.parse(in, mapper);
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parse JSON text into java object from the input source. Please use
	 * parseWithException() if you don't want to ignore the exception. if you
	 * want strict input check use parseStrict()
	 * 
	 * @see JSONParser#parse(String)
	 * @see #parseWithException(String)
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 * 
	 */
	public static Object parse(final String s) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(s);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Parse Json input to a java Object keeping element order
	 * 
	 * @since 1.0.6.1
	 */
	public static Object parseKeepingOrder(final Reader in) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, DefaultMapperOrdered.DEFAULT);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Parse Json input to a java Object keeping element order
	 * 
	 * @since 1.0.6.1
	 */
	public static Object parseKeepingOrder(final String in) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, DefaultMapperOrdered.DEFAULT);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Parse Json Using SAX event handler
	 * 
	 * @since 1.0.6.2
	 * @removed in 2.0
	 */
	// public static void SAXParse(String input, ContentHandler handler) throws
	// ParseException {
	// }

	/**
	 * Parse Json Using SAX event handler
	 * 
	 * @since 1.0.6.2
	 * @removed in 2.0
	 */
	// public static void SAXParse(Reader input, ContentHandler handler) throws
	// ParseException, IOException {
	// }

	/**
	 * Reformat Json input keeping element order
	 * 
	 * @since 1.0.6.2
	 * 
	 *        need to be rewrite in 2.0
	 */
	public static String compress(final String input, final JSONStyle style) {
		try {
			final StringBuilder sb = new StringBuilder();
			new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(input, new CompessorMapper(sb, style));
			return sb.toString();
		} catch (final Exception e) {
			e.printStackTrace();
			return input;
		}
	}

	/**
	 * Compress Json input keeping element order
	 * 
	 * @since 1.0.6.1
	 * 
	 *        need to be rewrite in 2.0
	 */
	public static String compress(final String input) {
		return compress(input, JSONStyle.MAX_COMPRESS);
	}

	/**
	 * Compress Json input keeping element order
	 * 
	 * @since 1.0.6.1
	 */
	public static String uncompress(final String input) {
		return compress(input, JSONStyle.NO_COMPRESS);
	}

	/**
	 * Parse JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseWithException(final byte[] in) throws IOException, ParseException {
		return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, DefaultMapper.DEFAULT);
	}

	/**
	 * Parse JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseWithException(final InputStream in) throws IOException, ParseException {
		return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, DefaultMapper.DEFAULT);
	}

	/**
	 * Parse JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseWithException(final Reader in) throws IOException, ParseException {
		return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, DefaultMapper.DEFAULT);
	}

	/**
	 * Parse JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseWithException(final String s) throws ParseException {
		return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(s, DefaultMapper.DEFAULT);
	}

	/**
	 * Parse valid RFC4627 JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseStrict(final Reader in) throws IOException, ParseException {
		return new JSONParser(MODE_RFC4627).parse(in, DefaultMapper.DEFAULT);
	}

	/**
	 * Parse valid RFC4627 JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseStrict(final String s) throws ParseException {
		return new JSONParser(MODE_RFC4627).parse(s, DefaultMapper.DEFAULT);
	}

	/**
	 * Check RFC4627 Json Syntax from input Reader
	 * 
	 * @return if the input is valid
	 */
	public static boolean isValidJsonStrict(final Reader in) throws IOException {
		try {
			new JSONParser(MODE_RFC4627).parse(in, FakeMapper.DEFAULT);
			return true;
		} catch (final ParseException e) {
			return false;
		}
	}

	/**
	 * check RFC4627 Json Syntax from input String
	 * 
	 * @return if the input is valid
	 */
	public static boolean isValidJsonStrict(final String s) {
		try {
			new JSONParser(MODE_RFC4627).parse(s, FakeMapper.DEFAULT);
			return true;
		} catch (final ParseException e) {
			return false;
		}
	}

	/**
	 * Check Json Syntax from input Reader
	 * 
	 * @return if the input is valid
	 */
	public static boolean isValidJson(final Reader in) throws IOException {
		try {
			new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, FakeMapper.DEFAULT);
			return true;
		} catch (final ParseException e) {
			return false;
		}
	}

	/**
	 * Check Json Syntax from input String
	 * 
	 * @return if the input is valid
	 */
	public static boolean isValidJson(final String s) {
		try {
			new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(s, FakeMapper.DEFAULT);
			return true;
		} catch (final ParseException e) {
			return false;
		}
	}

	/**
	 * Encode an object into JSON text and write it to out.
	 * <p>
	 * If this object is a Map or a List, and it's also a JSONStreamAware or a
	 * JSONAware, JSONStreamAware or JSONAware will be considered firstly.
	 * <p>
	 * 
	 * @see JSONObject#writeJSON(Map, Appendable)
	 * @see JSONArray#writeJSONString(List, Appendable)
	 */
	public static void writeJSONString(final Object value, final Appendable out) throws IOException {
		writeJSONString(value, out, COMPRESSION);
	}

	/**
	 * Encode an object into JSON text and write it to out.
	 * <p>
	 * If this object is a Map or a List, and it's also a JSONStreamAware or a
	 * JSONAware, JSONStreamAware or JSONAware will be considered firstly.
	 * <p>
	 * 
	 * @see JSONObject#writeJSON(Map, Appendable)
	 * @see JSONArray#writeJSONString(List, Appendable)
	 */
	@SuppressWarnings("unchecked")
	public static void writeJSONString(final Object value, final Appendable out,
			final JSONStyle compression) throws IOException {
		if (value == null) {
			out.append("null");
			return;
		}

		if (value instanceof String) {
			if (!compression.mustProtectValue((String) value)) {
				out.append((String) value);
			} else {
				out.append('"');
				escape((String) value, out, compression);
				out.append('"');
			}
			return;
		}

		if (value instanceof Number) {
			if (value instanceof Double) {
				if (((Double) value).isInfinite()) {
					out.append("null");
				} else {
					out.append(value.toString());
				}
			} else if (value instanceof Float) {
				if (((Float) value).isInfinite()) {
					out.append("null");
				} else {
					out.append(value.toString());
				}
			} else {
				out.append(value.toString());
			}
			return;
		}

		if (value instanceof Boolean) {
			out.append(value.toString());
		} else if ((value instanceof JSONStreamAware)) {
			if (value instanceof JSONStreamAwareEx) {
				((JSONStreamAwareEx) value).writeJSONString(out, compression);
			} else {
				((JSONStreamAware) value).writeJSONString(out);
			}
		} else if ((value instanceof JSONAware)) {
			if ((value instanceof JSONAwareEx)) {
				out.append(((JSONAwareEx) value).toJSONString(compression));
			} else {
				out.append(((JSONAware) value).toJSONString());
			}
		} else if (value instanceof Map<?, ?>) {
			JSONObject.writeJSON((Map<String, Object>) value, out, compression);
		} else if (value instanceof Iterable<?>) { // List
			JSONArray.writeJSONString((Iterable<Object>) value, out, compression);
		} else if (value instanceof Date) {
			JSONValue.writeJSONString(value.toString(), out, compression);
		} else if (value instanceof Enum<?>) {
			@SuppressWarnings("rawtypes")
			final String s = ((Enum) value).name();
			if (!compression.mustProtectValue(s)) {
				out.append(s);
			} else {
				out.append('"');
				escape(s, out, compression);
				out.append('"');
			}
			return;
		} else if (value.getClass().isArray()) {
			final Class<?> arrayClz = value.getClass();
			final Class<?> c = arrayClz.getComponentType();

			out.append('[');
			boolean needSep = false;

			if (c.isPrimitive()) {
				if (c == int.class) {
					for (final int b : ((int[]) value)) {
						if (needSep) {
							out.append(',');
						} else {
							needSep = true;
						}
						out.append(Integer.toString(b));
					}
				} else if (c == short.class) {
					for (final short b : ((short[]) value)) {
						if (needSep) {
							out.append(',');
						} else {
							needSep = true;
						}
						out.append(Short.toString(b));
					}
				} else if (c == byte.class) {
					for (final byte b : ((byte[]) value)) {
						if (needSep) {
							out.append(',');
						} else {
							needSep = true;
						}
						out.append(Integer.toString(b));
					}
				} else if (c == long.class) {
					for (final long b : ((long[]) value)) {
						if (needSep) {
							out.append(',');
						} else {
							needSep = true;
						}
						out.append(Long.toString(b));
					}
				} else if (c == float.class) {
					for (final float b : ((float[]) value)) {
						if (needSep) {
							out.append(',');
						} else {
							needSep = true;
						}
						out.append(Float.toString(b));
					}
				} else if (c == double.class) {
					for (final double b : ((double[]) value)) {
						if (needSep) {
							out.append(',');
						} else {
							needSep = true;
						}
						out.append(Double.toString(b));
					}
				} else if (c == boolean.class) {
					for (final boolean b : ((boolean[]) value)) {
						if (needSep) {
							out.append(',');
						} else {
							needSep = true;
						}
						if (b) {
							out.append("true");
						} else {
							out.append("false");
						}
					}
				}
			} else {
				for (final Object o : ((Object[]) value)) {
					if (needSep) {
						out.append(',');
					} else {
						needSep = true;
					}
					writeJSONString(o, out, compression);
				}
			}
			out.append(']');
		} else {
			try {
				final Class<?> cls = value.getClass();
				boolean needSep = false;
				final BeansAccess fields = BeansAccess.get(cls, JSONUtil.JSON_SMART_FIELD_FILTER);
				out.append('{');
				for (final Accessor field : fields.getAccessors()) {
					final Object v = fields.get(value, field.getIndex());
					if (needSep) {
						out.append(',');
					} else {
						needSep = true;
					}
					JSONObject.writeJSONKV(field.getName(), v, out, compression);
				}
				out.append('}');
			} catch (final IOException e) {
				throw e;
			}
		}
	}

	/**
	 * Encode an object into JSON text and write it to out.
	 * <p>
	 * If this object is a Map or a List, and it's also a JSONStreamAware or a
	 * JSONAware, JSONStreamAware or JSONAware will be considered firstly.
	 * <p>
	 * 
	 * @see JSONObject#writeJSON(Map, Appendable)
	 * @see JSONArray#writeJSONString(List, Appendable)
	 */
	public static String toJSONString(final Object value) {
		return toJSONString(value, COMPRESSION);
	}

	/**
	 * Convert an object to JSON text.
	 * <p>
	 * If this object is a Map or a List, and it's also a JSONAware, JSONAware
	 * will be considered firstly.
	 * <p>
	 * DO NOT call this method from toJSONString() of a class that implements
	 * both JSONAware and Map or List with "this" as the parameter, use
	 * JSONObject.toJSONString(Map) or JSONArray.toJSONString(List) instead.
	 * 
	 * @see JSONObject#toJSONString(Map)
	 * @see JSONArray#toJSONString(List)
	 * 
	 * @return JSON text, or "null" if value is null or it's an NaN or an INF
	 *         number.
	 */
	public static String toJSONString(final Object value, final JSONStyle compression) {
		final StringBuilder sb = new StringBuilder();
		try {
			writeJSONString(value, sb, compression);
		} catch (final IOException e) {
			// can not append on a StringBuilder
		}
		return sb.toString();
	}

	public static String escape(final String s) {
		return escape(s, COMPRESSION);
	}

	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters
	 * (U+0000 through U+001F).
	 */
	public static String escape(final String s, final JSONStyle compression) {
		if (s == null) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		compression.escape(s, sb);
		return sb.toString();
	}

	public static void escape(final String s, final Appendable ap) {
		escape(s, ap, COMPRESSION);
	}

	public static void escape(final String s, final Appendable ap, final JSONStyle compression) {
		if (s == null) {
			return;
		}
		compression.escape(s, ap);
	}
}

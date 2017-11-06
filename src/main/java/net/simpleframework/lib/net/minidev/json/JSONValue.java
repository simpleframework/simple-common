package net.simpleframework.lib.net.minidev.json;

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
import static net.simpleframework.lib.net.minidev.json.parser.JSONParser.DEFAULT_PERMISSIVE_MODE;
import static net.simpleframework.lib.net.minidev.json.parser.JSONParser.MODE_RFC4627;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import net.simpleframework.lib.net.minidev.json.parser.JSONParser;
import net.simpleframework.lib.net.minidev.json.parser.ParseException;
import net.simpleframework.lib.net.minidev.json.reader.JsonWriter;
import net.simpleframework.lib.net.minidev.json.reader.JsonWriterI;
import net.simpleframework.lib.net.minidev.json.writer.CompessorMapper;
import net.simpleframework.lib.net.minidev.json.writer.FakeMapper;
import net.simpleframework.lib.net.minidev.json.writer.JsonReader;
import net.simpleframework.lib.net.minidev.json.writer.JsonReaderI;
import net.simpleframework.lib.net.minidev.json.writer.UpdaterMapper;

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
			return p.parse(in, defaultReader.getMapper(mapTo));
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
			return p.parse(in, defaultReader.getMapper(mapTo));
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
			return p.parse(in, defaultReader.getMapper(mapTo));
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
			return p.parse(in, new UpdaterMapper<>(defaultReader, toUpdate));
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
	protected static <T> T parse(final Reader in, final JsonReaderI<T> mapper) {
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
			return p.parse(in, defaultReader.getMapper(mapTo));
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
			return p.parse(in, new UpdaterMapper<>(defaultReader, toUpdate));
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
			return p.parse(in, new UpdaterMapper<>(defaultReader, toUpdate));
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
	protected static <T> T parse(final byte[] in, final JsonReaderI<T> mapper) {
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
	protected static <T> T parse(final String in, final JsonReaderI<T> mapper) {
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
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, defaultReader.DEFAULT_ORDERED);
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
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, defaultReader.DEFAULT_ORDERED);
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
			new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(input,
					new CompessorMapper(defaultReader, sb, style));
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
		return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, defaultReader.DEFAULT);
	}

	/**
	 * Parse JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseWithException(final InputStream in)
			throws IOException, ParseException {
		return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, defaultReader.DEFAULT);
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
		return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, defaultReader.DEFAULT);
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
		return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(s, defaultReader.DEFAULT);
	}

	/**
	 * Parse input json as a mapTo class
	 * 
	 * mapTo can be a bean
	 * 
	 * @since 2.0
	 */
	public static <T> T parseWithException(final String in, final Class<T> mapTo)
			throws ParseException {
		final JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
		return p.parse(in, defaultReader.getMapper(mapTo));
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
		return new JSONParser(MODE_RFC4627).parse(in, defaultReader.DEFAULT);
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
		return new JSONParser(MODE_RFC4627).parse(s, defaultReader.DEFAULT);
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
	 * Serialisation class Data
	 */
	public final static JsonWriter defaultWriter = new JsonWriter();
	/**
	 * deserialisation class Data
	 */
	public final static JsonReader defaultReader = new JsonReader();

	/**
	 * remap field from java to json.
	 * 
	 * @since 2.1.1
	 */
	public static <T> void remapField(final Class<T> type, final String jsonFieldName,
			final String javaFieldName) {
		defaultReader.remapField(type, jsonFieldName, javaFieldName);
		defaultWriter.remapField(type, javaFieldName, jsonFieldName);
	}

	/**
	 * Register a serializer for a class.
	 */
	public static <T> void registerWriter(final Class<?> cls, final JsonWriterI<T> writer) {
		defaultWriter.registerWriter(writer, cls);
	}

	/**
	 * register a deserializer for a class.
	 */
	public static <T> void registerReader(final Class<T> type, final JsonReaderI<T> mapper) {
		defaultReader.registerReader(type, mapper);
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
		final Class<?> clz = value.getClass();
		@SuppressWarnings("rawtypes")
		JsonWriterI w = defaultWriter.getWrite(clz);
		if (w == null) {
			if (clz.isArray()) {
				w = JsonWriter.arrayWriter;
			} else {
				w = defaultWriter.getWriterByInterface(value.getClass());
				if (w == null) {
					w = JsonWriter.beansWriterASM;
					// w = JsonWriter.beansWriter;
				}
			}
			defaultWriter.registerWriter(w, clz);
		}
		w.writeJSONString(value, out, compression);
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

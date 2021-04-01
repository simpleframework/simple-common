package net.simpleframework.common;

import static net.simpleframework.lib.net.minidev.json.parser.JSONParser.DEFAULT_PERMISSIVE_MODE;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.simpleframework.common.logger.Log;
import net.simpleframework.common.logger.LogFactory;
import net.simpleframework.lib.net.minidev.asm.BeansAccess;
import net.simpleframework.lib.net.minidev.json.JSONArray;
import net.simpleframework.lib.net.minidev.json.JSONObject;
import net.simpleframework.lib.net.minidev.json.JSONStyle;
import net.simpleframework.lib.net.minidev.json.JSONUtil;
import net.simpleframework.lib.net.minidev.json.JSONValue;
import net.simpleframework.lib.net.minidev.json.parser.JSONParser;
import net.simpleframework.lib.net.minidev.json.parser.ParseException;
import net.simpleframework.lib.net.minidev.json.writer.JsonReaderI;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class JsonUtils {
	/*-------------------------------bean-to-json-------------------------------*/

	public static String toJSON(final Map<String, ?> data) {
		return JSONObject.toJSONString(data);
	}

	public static String toJSON(final Map<String, ?> data, final JSONStyle style) {
		return JSONObject.toJSONString(data, style);
	}

	public static String toJSON(final Iterable<?> data) {
		final StringBuilder sb = new StringBuilder();
		try {
			JSONArray.writeJSONString(data, sb, JSONValue.COMPRESSION);
		} catch (final IOException e) {
			log.warn(e);
		}
		return sb.toString();
	}

	/*-------------------------------json-to-bean-------------------------------*/

	public static Map<String, Object> toMap(final String json) {
		final Map<String, Object> map = toObject(json, JSONObject.class);
		return map != null ? map : new HashMap<String, Object>();
	}

	public static <T> T toObject(final String json, final Class<T> valueType) {
		return json == null ? null : JSONValue.parse(json, valueType);
	}

	public static List<Map<String, Object>> toList(final String json) {
		return toList(json, JSONObject.class);
	}

	private static JSONParser JSON_PARSER = new JSONParser(DEFAULT_PERMISSIVE_MODE);

	@SuppressWarnings("unchecked")
	public static <T, M extends T> List<T> toList(final String json, final Class<M> beanClass) {
		try {
			return (List<T>) JSON_PARSER.parse(json, new ListMapper<T>(beanClass));
		} catch (final ParseException e) {
			log.warn(e);
			return null;
		}
	}

	public static class ListMapper<T> extends JsonReaderI<T> {
		private final BeansAccess<?> ba;

		private final Class<?> valueClass;

		private JsonReaderI<?> subMapper;

		public ListMapper(final Class<?> valueClass) {
			this(JSONArray.class, valueClass);
		}

		public ListMapper(final Class<?> listClass, final Class<?> valueClass) {
			super(JSONValue.defaultReader);
			this.valueClass = valueClass;
			final Class<?> type = listClass.isInterface() ? JSONArray.class : listClass;
			ba = BeansAccess.get(type, JSONUtil.JSON_SMART_FIELD_FILTER);
		}

		@Override
		public Object createArray() {
			return ba.newInstance();
		}

		@Override
		public JsonReaderI<?> startObject(final String key) {
			if (subMapper == null) {
				subMapper = base.getMapper(valueClass);
			}
			return subMapper;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void addValue(final Object current, final Object value) {
			((List<Object>) current).add(JSONUtil.convertToX(value, valueClass));
		}
	};

	static final Log log = LogFactory.getLogger(JsonUtils.class);
}

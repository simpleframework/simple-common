package net.simpleframework.common;

import static net.simpleframework.lib.net.minidev.json.parser.JSONParser.DEFAULT_PERMISSIVE_MODE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.simpleframework.lib.net.minidev.asm.BeansAccess;
import net.simpleframework.lib.net.minidev.json.JSONArray;
import net.simpleframework.lib.net.minidev.json.JSONObject;
import net.simpleframework.lib.net.minidev.json.JSONStyle;
import net.simpleframework.lib.net.minidev.json.JSONUtil;
import net.simpleframework.lib.net.minidev.json.JSONValue;
import net.simpleframework.lib.net.minidev.json.mapper.AMapper;
import net.simpleframework.lib.net.minidev.json.mapper.Mapper;
import net.simpleframework.lib.net.minidev.json.parser.JSONParser;
import net.simpleframework.lib.net.minidev.json.parser.ParseException;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
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
		}
		return sb.toString();
	}

	/*-------------------------------json-to-bean-------------------------------*/

	public static <T> T toObject(final String json, final Class<T> valueType) {
		if (!StringUtils.hasText(json)) {
			return null;
		}
		return JSONValue.parse(json, valueType);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, ?> toMap(final String json) {
		return toObject(json, HashMap.class);
	}

	public static Collection<?> toList(final String json) {
		return toList(json, null);
	}

	public static List<?> toList(final String json, final Class<?> beanClass) {
		if (beanClass == null) {
			return JSONValue.parse(json, ArrayList.class);
		}
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(json, new ListMapper<List<?>>(
					beanClass));
		} catch (final ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static class TestBean {
		private ID id;

		public ID getId() {
			return id;
		}

		public void setId(final ID id) {
			this.id = id;
		}
	}

	public static class ListMapper<T> extends AMapper<T> {

		private final BeansAccess<?> ba;

		private final Class<?> valueClass;

		private AMapper<?> subMapper;

		public ListMapper(final Class<?> valueClass) {
			this(JSONArray.class, valueClass);
		}

		public ListMapper(final Class<?> listClass, final Class<?> valueClass) {
			this.valueClass = valueClass;
			ba = BeansAccess.get(listClass.isInterface() ? JSONArray.class : listClass,
					JSONUtil.JSON_SMART_FIELD_FILTER);
		}

		@Override
		public Object createArray() {
			return ba.newInstance();
		}

		@Override
		public AMapper<?> startObject(final String key) {
			if (subMapper == null) {
				subMapper = Mapper.getMapper(valueClass);
			}
			return subMapper;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void addValue(final Object current, final Object value) {
			((List<Object>) current).add(JSONUtil.convertToX(value, valueClass));
		}
	};
}

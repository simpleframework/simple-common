package net.simpleframework.lib.net.minidev.json.reader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.simpleframework.lib.net.minidev.asm.Accessor;
import net.simpleframework.lib.net.minidev.asm.BeansAccess;
import net.simpleframework.lib.net.minidev.json.JSONObject;
import net.simpleframework.lib.net.minidev.json.JSONStyle;
import net.simpleframework.lib.net.minidev.json.JSONUtil;

public class BeansWriterASMRemap implements JsonWriterI<Object> {
	private final Map<String, String> rename = new HashMap<>();

	public void renameField(final String source, final String dest) {
		rename.put(source, dest);
	}

	private String rename(final String key) {
		final String k2 = rename.get(key);
		if (k2 != null) {
			return k2;
		}
		return key;
	}

	@Override
	public <E> void writeJSONString(final E value, final Appendable out, final JSONStyle compression)
			throws IOException {
		try {
			final Class<?> cls = value.getClass();
			boolean needSep = false;
			@SuppressWarnings("rawtypes")
			final BeansAccess fields = BeansAccess.get(cls, JSONUtil.JSON_SMART_FIELD_FILTER);
			out.append('{');
			for (final Accessor field : fields.getAccessors()) {
				@SuppressWarnings("unchecked")
				final Object v = fields.get(value, field.getIndex());
				if (v == null && compression.ignoreNull()) {
					continue;
				}
				if (needSep) {
					out.append(',');
				} else {
					needSep = true;
				}
				String key = field.getName();
				key = rename(key);
				JSONObject.writeJSONKV(key, v, out, compression);
			}
			out.append('}');
		} catch (final IOException e) {
			throw e;
		}
	}

}

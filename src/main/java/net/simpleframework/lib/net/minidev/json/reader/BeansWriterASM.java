package net.simpleframework.lib.net.minidev.json.reader;

import java.io.IOException;

import net.simpleframework.lib.net.minidev.asm.Accessor;
import net.simpleframework.lib.net.minidev.asm.BeansAccess;
import net.simpleframework.lib.net.minidev.json.JSONObject;
import net.simpleframework.lib.net.minidev.json.JSONStyle;
import net.simpleframework.lib.net.minidev.json.JSONUtil;

public class BeansWriterASM implements JsonWriterI<Object> {
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
				final String key = field.getName();
				JSONObject.writeJSONKV(key, v, out, compression);
			}
			out.append('}');
		} catch (final IOException e) {
			throw e;
		}
	}
}

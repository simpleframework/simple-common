package net.simpleframework.lib.net.minidev.json.reader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.simpleframework.lib.net.minidev.json.JSONStyle;
import net.simpleframework.lib.net.minidev.json.JSONUtil;

public class BeansWriter implements JsonWriterI<Object> {
	@Override
	public <E> void writeJSONString(final E value, final Appendable out, final JSONStyle compression)
			throws IOException {
		try {
			Class<?> nextClass = value.getClass();
			boolean needSep = false;
			compression.objectStart(out);
			while (nextClass != Object.class) {
				final Field[] fields = nextClass.getDeclaredFields();
				for (final Field field : fields) {
					final int m = field.getModifiers();
					if ((m & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) > 0) {
						continue;
					}
					Object v = null;
					if ((m & Modifier.PUBLIC) > 0) {
						v = field.get(value);
					} else {
						String g = JSONUtil.getGetterName(field.getName());
						Method mtd = null;

						try {
							mtd = nextClass.getDeclaredMethod(g);
						} catch (final Exception e) {
						}
						if (mtd == null) {
							final Class<?> c2 = field.getType();
							if (c2 == Boolean.TYPE || c2 == Boolean.class) {
								g = JSONUtil.getIsName(field.getName());
								mtd = nextClass.getDeclaredMethod(g);
							}
						}
						if (mtd == null) {
							continue;
						}
						v = mtd.invoke(value);
					}
					if (v == null && compression.ignoreNull()) {
						continue;
					}
					if (needSep) {
						compression.objectNext(out);
					} else {
						needSep = true;
					}
					final String key = field.getName();

					JsonWriter.writeJSONKV(key, v, out, compression);
					// compression.objectElmStop(out);
				}
				nextClass = nextClass.getSuperclass();
			}
			compression.objectStop(out);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}

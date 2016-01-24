package net.simpleframework.lib.net.minidev.json.reader;

import java.io.IOException;

import net.simpleframework.lib.net.minidev.json.JSONStyle;
import net.simpleframework.lib.net.minidev.json.JSONValue;

public class ArrayWriter implements JsonWriterI<Object> {
	@Override
	public <E> void writeJSONString(final E value, final Appendable out, final JSONStyle compression)
			throws IOException {
		compression.arrayStart(out);
		boolean needSep = false;
		for (final Object o : ((Object[]) value)) {
			if (needSep) {
				compression.objectNext(out);
			} else {
				needSep = true;
			}
			JSONValue.writeJSONString(o, out, compression);
		}
		compression.arrayStop(out);
	}
}

package net.simpleframework.lib.net.minidev.json.mapper;

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
import java.io.IOException;

import net.simpleframework.lib.net.minidev.json.JSONStyle;
import net.simpleframework.lib.net.minidev.json.JSONValue;

public class CompessorMapper extends AMapper<CompessorMapper> {
	private final Appendable out;
	private final JSONStyle compression;
	private Boolean _isObj;
	private boolean needSep = false;
	private boolean isOpen = false;
	private boolean isClosed = false;

	// private boolean isRoot = false;

	private boolean isArray() {
		return _isObj == Boolean.FALSE;
	}

	private boolean isObject() {
		return _isObj == Boolean.TRUE;
	}

	private boolean isCompressor(final Object obj) {
		return obj instanceof CompessorMapper;
	}

	public CompessorMapper(final Appendable out, final JSONStyle compression) {
		this(out, compression, null);
		// isRoot = true;
	}

	public CompessorMapper(final Appendable out, final JSONStyle compression, final Boolean isObj) {
		this.out = out;
		this.compression = compression;
		this._isObj = isObj;
		// System.out.println("new CompressorMapper isObj:" + isObj);
	}

	@Override
	public AMapper<?> startObject(final String key) throws IOException {
		open(this);
		startKey(key);
		// System.out.println("startObject " + key);
		final CompessorMapper r = new CompessorMapper(out, compression, true);
		open(r);
		return r;
	}

	@Override
	public AMapper<?> startArray(final String key) throws IOException {
		open(this);
		startKey(key);
		// System.out.println("startArray " + key);
		final CompessorMapper r = new CompessorMapper(out, compression, false);
		open(r);
		return r;
	}

	private void startKey(final String key) throws IOException {
		addComma();
		// if (key == null)
		// return;
		if (isArray()) {
			return;
		}
		if (!compression.mustProtectKey(key)) {
			out.append(key);
		} else {
			out.append('"');
			JSONValue.escape(key, out, compression);
			out.append('"');
		}
		out.append(':');
	}

	@Override
	public void setValue(final Object current, final String key, final Object value)
			throws IOException {
		// System.out.println("setValue(" + key + "," + value + ")");
		// if comprossor => data allready writed
		if (isCompressor(value)) {
			addComma();
			return;
		}
		startKey(key);
		writeValue(value);
	}

	@Override
	public void addValue(final Object current, final Object value) throws IOException {
		// System.out.println("add value" + value);
		// if (!isCompressor(value))
		addComma();
		writeValue(value);
	}

	private void addComma() throws IOException {
		if (needSep) {
			out.append(',');
			// needSep = false;
		} else {
			needSep = true;
		}
	}

	private void writeValue(final Object value) throws IOException {
		if (value instanceof String) {
			if (!compression.mustProtectValue((String) value)) {
				out.append((String) value);
			} else {
				out.append('"');
				JSONValue.escape((String) value, out, compression);
				out.append('"');
			}
			// needSep = true;
		} else {
			if (isCompressor(value)) {
				close(value);
				// needSep = true;
			} else {
				JSONValue.writeJSONString(value, out, compression);
				// needSep = true;
			}
		}
	}

	@Override
	public Object createObject() {
		// System.out.println("createObject");
		this._isObj = true;
		try {
			open(this);
		} catch (final Exception e) {
		}
		// if (this.isUnknow() && isRoot) { // && isRoot
		// this._isObj = true;
		// try {
		// out.append('{'); // 1
		// } catch (Exception e) {
		// }
		// }
		return this;
	}

	@Override
	public Object createArray() {
		// System.out.println("createArray");
		this._isObj = false;
		try {
			open(this);
		} catch (final Exception e) {
		}
		return this;
	}

	@Override
	public CompessorMapper convert(final Object current) {
		try {
			close(current);
			return this;
		} catch (final Exception e) {
			return this;
		}
	}

	private void close(final Object obj) throws IOException {
		if (!isCompressor(obj)) {
			return;
		}
		if (((CompessorMapper) obj).isClosed) {
			return;
		}
		((CompessorMapper) obj).isClosed = true;
		if (((CompessorMapper) obj).isObject()) {
			// System.out.println("convert }");
			out.append('}');
			needSep = true;
		} else if (((CompessorMapper) obj).isArray()) {
			// System.out.println("convert ]");
			out.append(']');
			needSep = true;
		}
	}

	private void open(final Object obj) throws IOException {
		if (!isCompressor(obj)) {
			return;
		}
		if (((CompessorMapper) obj).isOpen) {
			return;
		}
		((CompessorMapper) obj).isOpen = true;
		if (((CompessorMapper) obj).isObject()) {
			// System.out.println("open {");
			out.append('{');
			needSep = false;
		} else if (((CompessorMapper) obj).isArray()) {
			// System.out.println("open [");
			out.append('[');
			needSep = false;
		}
	}

}

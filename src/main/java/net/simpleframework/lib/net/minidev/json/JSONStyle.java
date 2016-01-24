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
import java.io.IOException;

import net.simpleframework.lib.net.minidev.json.JStylerObj.MustProtect;
import net.simpleframework.lib.net.minidev.json.JStylerObj.StringProtector;

/**
 * JSONStyle object configure JSonSerializer reducing output size
 * 
 * @author Uriel Chemouni <uchemouni@gmail.com>
 */
public class JSONStyle {
	/**
	 * for advanced usage sample see
	 * 
	 * #see net.simpleframework.lib.net.minidev.json.test.TestCompressorFlags
	 */
	public final static int FLAG_PROTECT_KEYS = 1;
	public final static int FLAG_PROTECT_4WEB = 2;
	public final static int FLAG_PROTECT_VALUES = 4;
	/**
	 * AGRESSIVE have no effect without PROTECT_KEYS or PROTECT_VALUE
	 * 
	 * AGRESSIVE mode allows Json-smart to not protect String containing
	 * special chars
	 */
	public final static int FLAG_AGRESSIVE = 8;
	/**
	 * @since 2.1
	 */
	public final static int FLAG_IGNORE_NULL = 16;

	public final static JSONStyle NO_COMPRESS = new JSONStyle(0);
	public final static JSONStyle MAX_COMPRESS = new JSONStyle(-1);
	/**
	 * @since 1.0.9.1
	 */
	public final static JSONStyle LT_COMPRESS = new JSONStyle(FLAG_PROTECT_4WEB);

	private final boolean _protectKeys;
	private final boolean _protect4Web;
	private final boolean _protectValues;
	private final boolean _ignore_null;

	private MustProtect mpKey;
	private MustProtect mpValue;

	private StringProtector esc;

	public JSONStyle(final int FLAG) {
		_protectKeys = (FLAG & FLAG_PROTECT_KEYS) == 0;
		_protectValues = (FLAG & FLAG_PROTECT_VALUES) == 0;
		_protect4Web = (FLAG & FLAG_PROTECT_4WEB) == 0;
		_ignore_null = (FLAG & FLAG_IGNORE_NULL) > 0;

		MustProtect mp;
		if ((FLAG & FLAG_AGRESSIVE) > 0) {
			mp = JStylerObj.MP_AGGRESIVE;
		} else {
			mp = JStylerObj.MP_SIMPLE;
		}

		if (_protectValues) {
			mpValue = JStylerObj.MP_TRUE;
		} else {
			mpValue = mp;
		}

		if (_protectKeys) {
			mpKey = JStylerObj.MP_TRUE;
		} else {
			mpKey = mp;
		}

		if (_protect4Web) {
			esc = JStylerObj.ESCAPE4Web;
		} else {
			esc = JStylerObj.ESCAPE_LT;
		}
	}

	public JSONStyle() {
		this(0);
	}

	public boolean protectKeys() {
		return _protectKeys;
	}

	public boolean protectValues() {
		return _protectValues;
	}

	public boolean protect4Web() {
		return _protect4Web;
	}

	public boolean ignoreNull() {
		return _ignore_null;
	}

	public boolean indent() {
		return false;
	}

	public boolean mustProtectKey(final String s) {
		return mpKey.mustBeProtect(s);
	}

	public boolean mustProtectValue(final String s) {
		return mpValue.mustBeProtect(s);
	}

	public void writeString(final Appendable out, final String value) throws IOException {
		if (!this.mustProtectValue(value)) {
			out.append(value);
		} else {
			out.append('"');
			JSONValue.escape(value, out, this);
			out.append('"');
		}
	}

	public void escape(final String s, final Appendable out) {
		esc.escape(s, out);
	}

	/**
	 * begin Object
	 */
	public void objectStart(final Appendable out) throws IOException {
		out.append('{');
	}

	/**
	 * terminate Object
	 */
	public void objectStop(final Appendable out) throws IOException {
		out.append('}');
	}

	/**
	 * Start the first Obeject element
	 */
	public void objectFirstStart(final Appendable out) throws IOException {
	}

	/**
	 * Start a new Object element
	 */
	public void objectNext(final Appendable out) throws IOException {
		out.append(',');
	}

	/**
	 * End Of Object element
	 */
	public void objectElmStop(final Appendable out) throws IOException {
	}

	/**
	 * end of Key in json Object
	 */
	public void objectEndOfKey(final Appendable out) throws IOException {
		out.append(':');
	}

	/**
	 * Array start
	 */
	public void arrayStart(final Appendable out) throws IOException {
		out.append('[');
	}

	/**
	 * Array Done
	 */
	public void arrayStop(final Appendable out) throws IOException {
		out.append(']');
	}

	/**
	 * Start the first Array element
	 */
	public void arrayfirstObject(final Appendable out) throws IOException {
	}

	/**
	 * Start a new Array element
	 */
	public void arrayNextElm(final Appendable out) throws IOException {
		out.append(',');
	}

	/**
	 * End of an Array element
	 */
	public void arrayObjectEnd(final Appendable out) throws IOException {
	}
}

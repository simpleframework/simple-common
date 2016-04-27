package net.simpleframework.lib.org.jsoup.nodes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;

import net.simpleframework.lib.org.jsoup.SerializationException;
import net.simpleframework.lib.org.jsoup.helper.StringUtil;
import net.simpleframework.lib.org.jsoup.parser.Parser;

/**
 * HTML entities, and escape routines.
 * Source: <a href=
 * "http://www.w3.org/TR/html5/named-character-references.html#named-character-references"
 * >W3C HTML
 * named character references</a>.
 */
public class Entities {
	public enum EscapeMode {
		/**
		 * Restricted entities suitable for XHTML output: lt, gt, amp, and quot
		 * only.
		 */
		xhtml(xhtmlByVal),
		/** Default HTML output entities. */
		base(baseByVal),
		/** Complete HTML entities. */
		extended(fullByVal);

		private Map<Character, String> map;

		EscapeMode(final Map<Character, String> map) {
			this.map = map;
		}

		public Map<Character, String> getMap() {
			return map;
		}
	}

	private static final Map<String, Character> full;
	private static final Map<Character, String> xhtmlByVal;
	private static final Map<String, Character> base;
	private static final Map<Character, String> baseByVal;
	private static final Map<Character, String> fullByVal;

	private Entities() {
	}

	/**
	 * Check if the input is a known named entity
	 * 
	 * @param name
	 *        the possible entity name (e.g. "lt" or "amp")
	 * @return true if a known named entity
	 */
	public static boolean isNamedEntity(final String name) {
		return full.containsKey(name);
	}

	/**
	 * Check if the input is a known named entity in the base entity set.
	 * 
	 * @param name
	 *        the possible entity name (e.g. "lt" or "amp")
	 * @return true if a known named entity in the base set
	 * @see #isNamedEntity(String)
	 */
	public static boolean isBaseNamedEntity(final String name) {
		return base.containsKey(name);
	}

	/**
	 * Get the Character value of the named entity
	 * 
	 * @param name
	 *        named entity (e.g. "lt" or "amp")
	 * @return the Character value of the named entity (e.g. '{@literal <}' or '
	 *         {@literal &}')
	 */
	public static Character getCharacterByName(final String name) {
		return full.get(name);
	}

	static String escape(final String string, final Document.OutputSettings out) {
		final StringBuilder accum = new StringBuilder(string.length() * 2);
		try {
			escape(accum, string, out, false, false, false);
		} catch (final IOException e) {
			throw new SerializationException(e); // doesn't happen
		}
		return accum.toString();
	}

	// this method is ugly, and does a lot. but other breakups cause rescanning
	// and stringbuilder generations
	static void escape(final Appendable accum, final String string,
			final Document.OutputSettings out, final boolean inAttribute,
			final boolean normaliseWhite, final boolean stripLeadingWhite) throws IOException {

		boolean lastWasWhite = false;
		boolean reachedNonWhite = false;
		final EscapeMode escapeMode = out.escapeMode();
		final CharsetEncoder encoder = out.encoder();
		final CoreCharset coreCharset = CoreCharset.byName(encoder.charset().name());
		final Map<Character, String> map = escapeMode.getMap();
		final int length = string.length();

		int codePoint;
		for (int offset = 0; offset < length; offset += Character.charCount(codePoint)) {
			codePoint = string.codePointAt(offset);

			if (normaliseWhite) {
				if (StringUtil.isWhitespace(codePoint)) {
					if ((stripLeadingWhite && !reachedNonWhite) || lastWasWhite) {
						continue;
					}
					accum.append(' ');
					lastWasWhite = true;
					continue;
				} else {
					lastWasWhite = false;
					reachedNonWhite = true;
				}
			}
			// surrogate pairs, split implementation for efficiency on single char
			// common case (saves creating strings, char[]):
			if (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
				final char c = (char) codePoint;
				// html specific and required escapes:
				switch (c) {
				case '&':
					accum.append("&amp;");
					break;
				case 0xA0:
					if (escapeMode != EscapeMode.xhtml) {
						accum.append("&nbsp;");
					} else {
						accum.append("&#xa0;");
					}
					break;
				case '<':
					// escape when in character data or when in a xml attribue val;
					// not needed in html attr val
					if (!inAttribute || escapeMode == EscapeMode.xhtml) {
						accum.append("&lt;");
					} else {
						accum.append(c);
					}
					break;
				case '>':
					if (!inAttribute) {
						accum.append("&gt;");
					} else {
						accum.append(c);
					}
					break;
				case '"':
					if (inAttribute) {
						accum.append("&quot;");
					} else {
						accum.append(c);
					}
					break;
				default:
					if (canEncode(coreCharset, c, encoder)) {
						accum.append(c);
					} else if (map.containsKey(c)) {
						accum.append('&').append(map.get(c)).append(';');
					} else {
						accum.append("&#x").append(Integer.toHexString(codePoint)).append(';');
					}
				}
			} else {
				final String c = new String(Character.toChars(codePoint));
				if (encoder.canEncode(c)) {
					accum.append(c);
				} else {
					accum.append("&#x").append(Integer.toHexString(codePoint)).append(';');
				}
			}
		}
	}

	static String unescape(final String string) {
		return unescape(string, false);
	}

	/**
	 * Unescape the input string.
	 * 
	 * @param string
	 *        to un-HTML-escape
	 * @param strict
	 *        if "strict" (that is, requires trailing ';' char, otherwise that's
	 *        optional)
	 * @return unescaped string
	 */
	static String unescape(final String string, final boolean strict) {
		return Parser.unescapeEntities(string, strict);
	}

	/*
	 * Provides a fast-path for Encoder.canEncode, which drastically improves
	 * performance on Android post JellyBean.
	 * After KitKat, the implementation of canEncode degrades to the point of
	 * being useless. For non ASCII or UTF,
	 * performance may be bad. We can add more encoders for common character sets
	 * that are impacted by performance
	 * issues on Android if required.
	 * 
	 * Benchmarks: *
	 * OLD toHtml() impl v New (fastpath) in millis
	 * Wiki: 1895, 16
	 * CNN: 6378, 55
	 * Alterslash: 3013, 28
	 * Jsoup: 167, 2
	 */

	private static boolean canEncode(final CoreCharset charset, final char c,
			final CharsetEncoder fallback) {
		// todo add more charset tests if impacted by Android's bad perf in
		// canEncode
		switch (charset) {
		case ascii:
			return c < 0x80;
		case utf:
			return true; // real is:!(Character.isLowSurrogate(c) ||
								// Character.isHighSurrogate(c)); - but already check
								// above
		default:
			return fallback.canEncode(c);
		}
	}

	private enum CoreCharset {
		ascii, utf, fallback;

		private static CoreCharset byName(final String name) {
			if (name.equals("US-ASCII")) {
				return ascii;
			}
			if (name.startsWith("UTF-")) {
				return utf;
			}
			return fallback;
		}
	}

	// xhtml has restricted entities
	private static final Object[][] xhtmlArray = { { "quot", 0x00022 }, { "amp", 0x00026 },
			{ "lt", 0x0003C }, { "gt", 0x0003E } };

	static {
		xhtmlByVal = new HashMap<Character, String>();
		base = loadEntities("entities-base.properties"); // most common / default
		baseByVal = toCharacterKey(base);
		full = loadEntities("entities-full.properties"); // extended and
																			// overblown.
		fullByVal = toCharacterKey(full);

		for (final Object[] entity : xhtmlArray) {
			final Character c = Character.valueOf((char) ((Integer) entity[1]).intValue());
			xhtmlByVal.put(c, ((String) entity[0]));
		}
	}

	private static Map<String, Character> loadEntities(final String filename) {
		final Properties properties = new Properties();
		final Map<String, Character> entities = new HashMap<String, Character>();
		try {
			final InputStream in = Entities.class.getResourceAsStream(filename);
			properties.load(in);
			in.close();
		} catch (final IOException e) {
			throw new MissingResourceException("Error loading entities resource: " + e.getMessage(),
					"Entities", filename);
		}

		for (final Map.Entry entry : properties.entrySet()) {
			final Character val = Character.valueOf((char) Integer.parseInt((String) entry.getValue(),
					16));
			final String name = (String) entry.getKey();
			entities.put(name, val);
		}
		return entities;
	}

	private static Map<Character, String> toCharacterKey(final Map<String, Character> inMap) {
		final Map<Character, String> outMap = new HashMap<Character, String>();
		for (final Map.Entry<String, Character> entry : inMap.entrySet()) {
			final Character character = entry.getValue();
			final String name = entry.getKey();

			if (outMap.containsKey(character)) {
				// dupe, prefer the lower case version
				if (name.toLowerCase().equals(name)) {
					outMap.put(character, name);
				}
			} else {
				outMap.put(character, name);
			}
		}
		return outMap;
	}
}

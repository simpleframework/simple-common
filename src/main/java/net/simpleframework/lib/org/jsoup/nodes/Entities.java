package net.simpleframework.lib.org.jsoup.nodes;

import static net.simpleframework.lib.org.jsoup.nodes.Entities.EscapeMode.base;
import static net.simpleframework.lib.org.jsoup.nodes.Entities.EscapeMode.extended;

import java.io.IOException;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.HashMap;

import net.simpleframework.lib.org.jsoup.SerializationException;
import net.simpleframework.lib.org.jsoup.helper.StringUtil;
import net.simpleframework.lib.org.jsoup.helper.Validate;
import net.simpleframework.lib.org.jsoup.parser.CharacterReader;
import net.simpleframework.lib.org.jsoup.parser.Parser;

/**
 * HTML entities, and escape routines. Source: <a href=
 * "http://www.w3.org/TR/html5/named-character-references.html#named-character-references">W3C
 * HTML named character references</a>.
 */
public class Entities {
	private static final int empty = -1;
	private static final String emptyName = "";
	static final int codepointRadix = 36;
	private static final char[] codeDelims = { ',', ';' };
	private static final HashMap<String, String> multipoints = new HashMap<>(); // name
																											// ->
																											// multiple
																											// character
																											// references
	private static final Document.OutputSettings DefaultOutput = new Document.OutputSettings();

	public enum EscapeMode {
		/**
		 * Restricted entities suitable for XHTML output: lt, gt, amp, and quot
		 * only.
		 */
		xhtml(EntitiesData.xmlPoints, 4),
		/**
		 * Default HTML output entities.
		 */
		base(EntitiesData.basePoints, 106),
		/**
		 * Complete HTML entities.
		 */
		extended(EntitiesData.fullPoints, 2125);

		// table of named references to their codepoints. sorted so we can binary
		// search. built by BuildEntities.
		private String[] nameKeys;
		private int[] codeVals; // limitation is the few references with multiple
										// characters; those go into multipoints.

		// table of codepoints to named entities.
		private int[] codeKeys; // we don' support multicodepoints to single named
										// value currently
		private String[] nameVals;

		EscapeMode(final String file, final int size) {
			load(this, file, size);
		}

		int codepointForName(final String name) {
			final int index = Arrays.binarySearch(nameKeys, name);
			return index >= 0 ? codeVals[index] : empty;
		}

		String nameForCodepoint(final int codepoint) {
			final int index = Arrays.binarySearch(codeKeys, codepoint);
			if (index >= 0) {
				// the results are ordered so lower case versions of same codepoint
				// come after uppercase, and we prefer to emit lower
				// (and binary search for same item with multi results is undefined
				return (index < nameVals.length - 1 && codeKeys[index + 1] == codepoint)
						? nameVals[index + 1]
						: nameVals[index];
			}
			return emptyName;
		}

		private int size() {
			return nameKeys.length;
		}
	}

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
		return extended.codepointForName(name) != empty;
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
		return base.codepointForName(name) != empty;
	}

	/**
	 * Get the Character value of the named entity
	 *
	 * @param name
	 *        named entity (e.g. "lt" or "amp")
	 * @return the Character value of the named entity (e.g. '{@literal <}' or
	 *         '{@literal &}')
	 * @deprecated does not support characters outside the BMP or multiple
	 *             character names
	 */
	@Deprecated
	public static Character getCharacterByName(final String name) {
		return (char) extended.codepointForName(name);
	}

	/**
	 * Get the character(s) represented by the named entity
	 *
	 * @param name
	 *        entity (e.g. "lt" or "amp")
	 * @return the string value of the character(s) represented by this entity,
	 *         or "" if not defined
	 */
	public static String getByName(final String name) {
		final String val = multipoints.get(name);
		if (val != null) {
			return val;
		}
		final int codepoint = extended.codepointForName(name);
		if (codepoint != empty) {
			return new String(new int[] { codepoint }, 0, 1);
		}
		return emptyName;
	}

	public static int codepointsForName(final String name, final int[] codepoints) {
		final String val = multipoints.get(name);
		if (val != null) {
			codepoints[0] = val.codePointAt(0);
			codepoints[1] = val.codePointAt(1);
			return 2;
		}
		final int codepoint = extended.codepointForName(name);
		if (codepoint != empty) {
			codepoints[0] = codepoint;
			return 1;
		}
		return 0;
	}

	/**
	 * HTML escape an input string. That is, {@code <} is returned as
	 * {@code &lt;}
	 *
	 * @param string
	 *        the un-escaped string to escape
	 * @param out
	 *        the output settings to use
	 * @return the escaped string
	 */
	public static String escape(final String string, final Document.OutputSettings out) {
		if (string == null) {
			return "";
		}
		final StringBuilder accum = new StringBuilder(string.length() * 2);
		try {
			escape(accum, string, out, false, false, false);
		} catch (final IOException e) {
			throw new SerializationException(e); // doesn't happen
		}
		return accum.toString();
	}

	/**
	 * HTML escape an input string, using the default settings (UTF-8, base
	 * entities). That is, {@code <} is returned as
	 * {@code &lt;}
	 *
	 * @param string
	 *        the un-escaped string to escape
	 * @return the escaped string
	 */
	public static String escape(final String string) {
		return escape(string, DefaultOutput);
	}

	// this method is ugly, and does a lot. but other breakups cause rescanning
	// and stringbuilder generations
	static void escape(final Appendable accum, final String string,
			final Document.OutputSettings out, final boolean inAttribute, final boolean normaliseWhite,
			final boolean stripLeadingWhite) throws IOException {

		boolean lastWasWhite = false;
		boolean reachedNonWhite = false;
		final EscapeMode escapeMode = out.escapeMode();
		final CharsetEncoder encoder = out.encoder();
		final CoreCharset coreCharset = out.coreCharset; // init in
																			// out.prepareEncoder()
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
					// ckan77
					final String name = base.nameForCodepoint(c);
					if (!emptyName.equals(name)) {
						accum.append('&').append(name).append(';');
					} else if (canEncode(coreCharset, c, encoder)) {
						accum.append(c);
					} else {
						appendEncoded(accum, escapeMode, codePoint);
					}
				}
			} else {
				final String c = new String(Character.toChars(codePoint));
				if (encoder.canEncode(c)) {
					accum.append(c);
				} else {
					appendEncoded(accum, escapeMode, codePoint);
				}
			}
		}
	}

	private static void appendEncoded(final Appendable accum, final EscapeMode escapeMode,
			final int codePoint) throws IOException {
		final String name = escapeMode.nameForCodepoint(codePoint);
		if (name != emptyName) {
			accum.append('&').append(name).append(';');
		} else {
			accum.append("&#x").append(Integer.toHexString(codePoint)).append(';');
		}
	}

	/**
	 * Un-escape an HTML escaped string. That is, {@code &lt;} is returned as
	 * {@code <}.
	 *
	 * @param string
	 *        the HTML string to un-escape
	 * @return the unescaped string
	 */
	public static String unescape(final String string) {
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

	enum CoreCharset {
		ascii, utf, fallback;

		static CoreCharset byName(final String name) {
			if (name.equals("US-ASCII")) {
				return ascii;
			}
			if (name.startsWith("UTF-")) {
				return utf;
			}
			return fallback;
		}
	}

	private static void load(final EscapeMode e, final String pointsData, final int size) {
		e.nameKeys = new String[size];
		e.codeVals = new int[size];
		e.codeKeys = new int[size];
		e.nameVals = new String[size];

		int i = 0;
		final CharacterReader reader = new CharacterReader(pointsData);

		while (!reader.isEmpty()) {
			// NotNestedLessLess=10913,824;1887&

			final String name = reader.consumeTo('=');
			reader.advance();
			final int cp1 = Integer.parseInt(reader.consumeToAny(codeDelims), codepointRadix);
			final char codeDelim = reader.current();
			reader.advance();
			final int cp2;
			if (codeDelim == ',') {
				cp2 = Integer.parseInt(reader.consumeTo(';'), codepointRadix);
				reader.advance();
			} else {
				cp2 = empty;
			}
			final String indexS = reader.consumeTo('&');
			final int index = Integer.parseInt(indexS, codepointRadix);
			reader.advance();

			e.nameKeys[i] = name;
			e.codeVals[i] = cp1;
			e.codeKeys[index] = cp1;
			e.nameVals[index] = name;

			if (cp2 != empty) {
				multipoints.put(name, new String(new int[] { cp1, cp2 }, 0, 2));
			}
			i++;
		}

		Validate.isTrue(i == size, "Unexpected count of entities loaded");
	}
}

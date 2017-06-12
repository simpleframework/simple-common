package net.simpleframework.lib.org.jsoup.parser;

import java.util.Arrays;
import java.util.Locale;

import net.simpleframework.lib.org.jsoup.helper.Validate;

/**
 * CharacterReader consumes tokens off a string. Used internally by jsoup. API
 * subject to changes.
 */
public final class CharacterReader {
	static final char EOF = (char) -1;
	private static final int maxCacheLen = 12;

	private final char[] input;
	private final int length;
	private int pos = 0;
	private int mark = 0;
	private final String[] stringCache = new String[512]; // holds reused strings
																			// in this doc, to
																			// lessen garbage

	public CharacterReader(final String input) {
		Validate.notNull(input);
		this.input = input.toCharArray();
		this.length = this.input.length;
	}

	/**
	 * Gets the current cursor position in the content.
	 * 
	 * @return current position
	 */
	public int pos() {
		return pos;
	}

	/**
	 * Tests if all the content has been read.
	 * 
	 * @return true if nothing left to read.
	 */
	public boolean isEmpty() {
		return pos >= length;
	}

	/**
	 * Get the char at the current position.
	 * 
	 * @return char
	 */
	public char current() {
		return pos >= length ? EOF : input[pos];
	}

	char consume() {
		final char val = pos >= length ? EOF : input[pos];
		pos++;
		return val;
	}

	void unconsume() {
		pos--;
	}

	/**
	 * Moves the current position by one.
	 */
	public void advance() {
		pos++;
	}

	void mark() {
		mark = pos;
	}

	void rewindToMark() {
		pos = mark;
	}

	String consumeAsString() {
		return new String(input, pos++, 1);
	}

	/**
	 * Returns the number of characters between the current position and the next
	 * instance of the input char
	 * 
	 * @param c
	 *        scan target
	 * @return offset between current position and next instance of target. -1 if
	 *         not found.
	 */
	int nextIndexOf(final char c) {
		// doesn't handle scanning for surrogates
		for (int i = pos; i < length; i++) {
			if (c == input[i]) {
				return i - pos;
			}
		}
		return -1;
	}

	/**
	 * Returns the number of characters between the current position and the next
	 * instance of the input sequence
	 *
	 * @param seq
	 *        scan target
	 * @return offset between current position and next instance of target. -1 if
	 *         not found.
	 */
	int nextIndexOf(final CharSequence seq) {
		// doesn't handle scanning for surrogates
		final char startChar = seq.charAt(0);
		for (int offset = pos; offset < length; offset++) {
			// scan to first instance of startchar:
			if (startChar != input[offset]) {
				while (++offset < length && startChar != input[offset]) {
					/* empty */ }
			}
			int i = offset + 1;
			final int last = i + seq.length() - 1;
			if (offset < length && last <= length) {
				for (int j = 1; i < last && seq.charAt(j) == input[i]; i++, j++) {
					/* empty */ }
				if (i == last) {
					return offset - pos;
				}
			}
		}
		return -1;
	}

	/**
	 * Reads characters up to the specific char.
	 * 
	 * @param c
	 *        the delimiter
	 * @return the chars read
	 */
	public String consumeTo(final char c) {
		final int offset = nextIndexOf(c);
		if (offset != -1) {
			final String consumed = cacheString(pos, offset);
			pos += offset;
			return consumed;
		} else {
			return consumeToEnd();
		}
	}

	String consumeTo(final String seq) {
		final int offset = nextIndexOf(seq);
		if (offset != -1) {
			final String consumed = cacheString(pos, offset);
			pos += offset;
			return consumed;
		} else {
			return consumeToEnd();
		}
	}

	/**
	 * Read characters until the first of any delimiters is found.
	 * 
	 * @param chars
	 *        delimiters to scan for
	 * @return characters read up to the matched delimiter.
	 */
	public String consumeToAny(final char... chars) {
		final int start = pos;
		final int remaining = length;
		final char[] val = input;

		OUTER: while (pos < remaining) {
			for (final char c : chars) {
				if (val[pos] == c) {
					break OUTER;
				}
			}
			pos++;
		}

		return pos > start ? cacheString(start, pos - start) : "";
	}

	String consumeToAnySorted(final char... chars) {
		final int start = pos;
		final int remaining = length;
		final char[] val = input;

		while (pos < remaining) {
			if (Arrays.binarySearch(chars, val[pos]) >= 0) {
				break;
			}
			pos++;
		}

		return pos > start ? cacheString(start, pos - start) : "";
	}

	String consumeData() {
		// &, <, null
		final int start = pos;
		final int remaining = length;
		final char[] val = input;

		while (pos < remaining) {
			final char c = val[pos];
			if (c == '&' || c == '<' || c == TokeniserState.nullChar) {
				break;
			}
			pos++;
		}

		return pos > start ? cacheString(start, pos - start) : "";
	}

	String consumeTagName() {
		// '\t', '\n', '\r', '\f', ' ', '/', '>', nullChar
		final int start = pos;
		final int remaining = length;
		final char[] val = input;

		while (pos < remaining) {
			final char c = val[pos];
			if (c == '\t' || c == '\n' || c == '\r' || c == '\f' || c == ' ' || c == '/' || c == '>'
					|| c == TokeniserState.nullChar) {
				break;
			}
			pos++;
		}

		return pos > start ? cacheString(start, pos - start) : "";
	}

	String consumeToEnd() {
		final String data = cacheString(pos, length - pos);
		pos = length;
		return data;
	}

	String consumeLetterSequence() {
		final int start = pos;
		while (pos < length) {
			final char c = input[pos];
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || Character.isLetter(c)) {
				pos++;
			} else {
				break;
			}
		}

		return cacheString(start, pos - start);
	}

	String consumeLetterThenDigitSequence() {
		final int start = pos;
		while (pos < length) {
			final char c = input[pos];
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || Character.isLetter(c)) {
				pos++;
			} else {
				break;
			}
		}
		while (!isEmpty()) {
			final char c = input[pos];
			if (c >= '0' && c <= '9') {
				pos++;
			} else {
				break;
			}
		}

		return cacheString(start, pos - start);
	}

	String consumeHexSequence() {
		final int start = pos;
		while (pos < length) {
			final char c = input[pos];
			if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')) {
				pos++;
			} else {
				break;
			}
		}
		return cacheString(start, pos - start);
	}

	String consumeDigitSequence() {
		final int start = pos;
		while (pos < length) {
			final char c = input[pos];
			if (c >= '0' && c <= '9') {
				pos++;
			} else {
				break;
			}
		}
		return cacheString(start, pos - start);
	}

	boolean matches(final char c) {
		return !isEmpty() && input[pos] == c;

	}

	boolean matches(final String seq) {
		final int scanLength = seq.length();
		if (scanLength > length - pos) {
			return false;
		}

		for (int offset = 0; offset < scanLength; offset++) {
			if (seq.charAt(offset) != input[pos + offset]) {
				return false;
			}
		}
		return true;
	}

	boolean matchesIgnoreCase(final String seq) {
		final int scanLength = seq.length();
		if (scanLength > length - pos) {
			return false;
		}

		for (int offset = 0; offset < scanLength; offset++) {
			final char upScan = Character.toUpperCase(seq.charAt(offset));
			final char upTarget = Character.toUpperCase(input[pos + offset]);
			if (upScan != upTarget) {
				return false;
			}
		}
		return true;
	}

	boolean matchesAny(final char... seq) {
		if (isEmpty()) {
			return false;
		}

		final char c = input[pos];
		for (final char seek : seq) {
			if (seek == c) {
				return true;
			}
		}
		return false;
	}

	boolean matchesAnySorted(final char[] seq) {
		return !isEmpty() && Arrays.binarySearch(seq, input[pos]) >= 0;
	}

	boolean matchesLetter() {
		if (isEmpty()) {
			return false;
		}
		final char c = input[pos];
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || Character.isLetter(c);
	}

	boolean matchesDigit() {
		if (isEmpty()) {
			return false;
		}
		final char c = input[pos];
		return (c >= '0' && c <= '9');
	}

	boolean matchConsume(final String seq) {
		if (matches(seq)) {
			pos += seq.length();
			return true;
		} else {
			return false;
		}
	}

	boolean matchConsumeIgnoreCase(final String seq) {
		if (matchesIgnoreCase(seq)) {
			pos += seq.length();
			return true;
		} else {
			return false;
		}
	}

	boolean containsIgnoreCase(final String seq) {
		// used to check presence of </title>, </style>. only finds consistent
		// case.
		final String loScan = seq.toLowerCase(Locale.ENGLISH);
		final String hiScan = seq.toUpperCase(Locale.ENGLISH);
		return (nextIndexOf(loScan) > -1) || (nextIndexOf(hiScan) > -1);
	}

	@Override
	public String toString() {
		return new String(input, pos, length - pos);
	}

	/**
	 * Caches short strings, as a flywheel pattern, to reduce GC load. Just for
	 * this doc, to prevent leaks.
	 * <p />
	 * Simplistic, and on hash collisions just falls back to creating a new
	 * string, vs a full HashMap with Entry list.
	 * That saves both having to create objects as hash keys, and running through
	 * the entry list, at the expense of
	 * some more duplicates.
	 */
	private String cacheString(final int start, final int count) {
		final char[] val = input;
		final String[] cache = stringCache;

		// limit (no cache):
		if (count > maxCacheLen) {
			return new String(val, start, count);
		}

		// calculate hash:
		int hash = 0;
		int offset = start;
		for (int i = 0; i < count; i++) {
			hash = 31 * hash + val[offset++];
		}

		// get from cache
		final int index = hash & cache.length - 1;
		String cached = cache[index];

		if (cached == null) { // miss, add
			cached = new String(val, start, count);
			cache[index] = cached;
		} else { // hashcode hit, check equality
			if (rangeEquals(start, count, cached)) { // hit
				return cached;
			} else { // hashcode conflict
				cached = new String(val, start, count);
				cache[index] = cached; // update the cache, as recently used strings
												// are more likely to show up again
			}
		}
		return cached;
	}

	/**
	 * Check if the value of the provided range equals the string.
	 */
	boolean rangeEquals(final int start, int count, final String cached) {
		if (count == cached.length()) {
			final char one[] = input;
			int i = start;
			int j = 0;
			while (count-- != 0) {
				if (one[i++] != cached.charAt(j++)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}

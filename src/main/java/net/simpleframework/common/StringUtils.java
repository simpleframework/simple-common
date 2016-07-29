package net.simpleframework.common;

import java.util.Random;

import net.simpleframework.common.coll.ArrayUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class StringUtils {

	public static String replace(final String s, final String sub, final String with) {
		int c = 0;
		int i = s.indexOf(sub, c);
		if (i == -1) {
			return s;
		}
		final int length = s.length();
		final StringBuilder sb = new StringBuilder(length + with.length());
		do {
			sb.append(s.substring(c, i));
			sb.append(with);
			c = i + sub.length();
		} while ((i = s.indexOf(sub, c)) != -1);
		if (c < length) {
			sb.append(s.substring(c, length));
		}
		return sb.toString();
	}

	private static final String defaultDelimiter = ";";

	private static String[] BLANK_STRARR = new String[0];

	public static String[] split(final String src, final String delim) {
		if (isBlank(src)) {
			return BLANK_STRARR;
		}

		final int maxparts = (src.length() / delim.length()) + 2;
		final int[] positions = new int[maxparts];
		final int dellen = delim.length();

		int i, j = 0;
		int count = 0;
		positions[0] = -dellen;
		while ((i = src.indexOf(delim, j)) != -1) {
			count++;
			positions[count] = i;
			j = i + dellen;
		}
		count++;
		positions[count] = src.length();

		final String[] result = new String[count];

		for (i = 0; i < count; i++) {
			result[i] = src.substring(positions[i] + dellen, positions[i + 1]);
		}
		return result;
	}

	public static String[] split(final String str) {
		return split(str, defaultDelimiter);
	}

	public static String join(final Iterable<?> it, final String delim) {
		if (it == null) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		for (final Object o : it) {
			if (o == null) {
				continue;
			}
			if (sb.length() > 0) {
				if (delim != null) {
					sb.append(delim);
				}
			}
			sb.append(o);
		}
		return sb.toString();
	}

	public static String join(final Object[] arr, final String delim) {
		return join(ArrayUtils.asList(arr), delim);
	}

	public static String join(final Iterable<?> it) {
		return join(it, defaultDelimiter);
	}

	public static boolean isBlank(final String cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(cs.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasText(final String cs) {
		return !isBlank(cs);
	}

	public static boolean hasObject(final Object object) {
		if (object == null) {
			return false;
		}
		if (object instanceof String) {
			return hasText((String) object);
		}
		return true;
	}

	public static String text(final String... strings) {
		if (strings != null) {
			for (final String string : strings) {
				if (hasText(string)) {
					return string;
				}
			}
		}
		return "";
	}

	public static String blank(final Object object) {
		return object == null ? "" : Convert.toString(object);
	}

	private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
			'B', 'C', 'D', 'E', 'F' };

	public static String encodeHex(final byte[] binaryData) {
		final int l = binaryData.length;
		final char[] out = new char[l << 1];
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = DIGITS[(0xF0 & binaryData[i]) >>> 4];
			out[j++] = DIGITS[0x0F & binaryData[i]];
		}
		return new String(out);
	}

	public static byte[] decodeHex(final String encoded) {
		final char[] data = encoded.toCharArray();
		final int len = data.length;
		final byte[] out = new byte[len >> 1];
		for (int i = 0, j = 0; j < len; i++) {
			int f = Character.digit(data[j], 16) << 4;
			j++;
			f = f | Character.digit(data[j], 16);
			j++;
			out[i] = (byte) (f & 0xFF);
		}
		return out;
	}

	public static String decodeHexString(final String s) {
		if (s == null) {
			return null;
		}
		return new String(decodeHex(s));
	}

	public static String substring(final String str, final int length) {
		return substring(str, length, false);
	}

	public static String substring(String str, final int length, final boolean dot) {
		str = blank(str).trim();
		if (length >= str.length()) {
			return str;
		} else {
			str = str.substring(0, length);
			if (dot) {
				str += "...";
			}
			return str;
		}
	}

	public static int charCount(final String s, final char c) {
		int count = 0;
		if (s != null) {
			for (int i = 0; i < s.length(); i++) {
				if (s.charAt(i) == c) {
					count++;
				}
			}
		}
		return count;
	}

	public static String trimOneLine(String str) {
		for (final String c : new String[] { "\n", "\r", "\t" }) {
			str = replace(str, c, "");
		}
		return str.trim();
	}

	public static String trimLeadingWhitespace(final String str) {
		if (!hasText(str)) {
			return str;
		}
		final StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}

	public static String trimTrailingWhitespace(final String str) {
		if (!hasText(str)) {
			return str;
		}
		final StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public static String trimLeadingCharacter(final String str, final char leadingCharacter) {
		if (!hasText(str)) {
			return str;
		}
		final StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(0) == leadingCharacter) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}

	public static String trimTrailingCharacter(final String str, final char trailingCharacter) {
		if (!hasText(str)) {
			return str;
		}
		final StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(sb.length() - 1) == trailingCharacter) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	static final char[] RANDOM_NUMs = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
			'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
			'4', '5', '6', '7', '8', '9' };

	public static String genRandomNum(final int len) {
		// 35是因为数组是从0开始的，26个字母+10个数字
		final int maxl = RANDOM_NUMs.length;
		final StringBuffer sb = new StringBuffer();
		final Random r = new Random();
		int count = 0; // 生成的密码的长度
		while (count < len) {
			// 生成随机数，取绝对值，防止生成负数，
			final int i = Math.abs(r.nextInt(maxl)); // 生成的数最大为36-1
			if (i >= 0 && i < maxl) {
				sb.append(RANDOM_NUMs[i]);
				count++;
			}
		}
		return sb.toString();
	}
}

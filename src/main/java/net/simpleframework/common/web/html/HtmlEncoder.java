package net.simpleframework.common.web.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.simpleframework.common.StringUtils;

public class HtmlEncoder implements HtmlConst {

	protected static final char[][] TEXT = new char[64][];
	protected static final char[][] BLOCK = new char[64][];

	static {
		for (int i = 0; i < 64; i++) {
			TEXT[i] = new char[] { (char) i };
		}

		// special HTML characters
		TEXT['\''] = "&#039;".toCharArray();

		TEXT['"'] = QUOT.toCharArray();
		TEXT['&'] = AMP.toCharArray();
		TEXT['<'] = LT.toCharArray();
		TEXT['>'] = GT.toCharArray();

		// text table
		System.arraycopy(TEXT, 0, BLOCK, 0, 64);
		BLOCK['\n'] = "<br/>".toCharArray();
		BLOCK['\r'] = "<br/>".toCharArray();
	}

	public static String text(final String text) {
		int len;
		if ((text == null) || ((len = text.length()) == 0)) {
			return "";
		}
		final StringBuilder buffer = new StringBuilder(len + (len >> 2));
		for (int i = 0; i < len; i++) {
			final char c = text.charAt(i);
			if (c < 64) {
				buffer.append(TEXT[c]);
			} else {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

	public static String block(final String text) {
		int len;
		if ((text == null) || ((len = text.length()) == 0)) {
			return "";
		}
		final StringBuilder buffer = new StringBuilder(len + (len >> 2));
		char c, prev = 0;
		for (int i = 0; i < len; i++, prev = c) {
			c = text.charAt(i);
			if ((c == '\n') && (prev == '\r')) {
				continue; // previously '\r' (CR) was encoded, so skip '\n' (LF)
			}
			if (c < 64) {
				buffer.append(BLOCK[c]);
			} else {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

	public static String strict(final String text) {
		int len;
		if ((text == null) || ((len = text.length()) == 0)) {
			return "";
		}
		final StringBuilder buffer = new StringBuilder(len + (len >> 2));
		char c, prev = 0;
		boolean prevSpace = false;
		for (int i = 0; i < len; i++, prev = c) {
			c = text.charAt(i);
			if (c == ' ') {
				if (prev != ' ') {
					prevSpace = false;
				}
				if (prevSpace == false) {
					buffer.append(' ');
				} else {
					buffer.append(NBSP);
				}
				prevSpace = !prevSpace;
				continue;
			}
			if ((c == '\n') && (prev == '\r')) {
				continue; // previously '\r' (CR) was encoded, so skip '\n' (LF)
			}
			if (c < 64) {
				buffer.append(BLOCK[c]);
			} else {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

	static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*?>.*?<\\/script>");

	public static String script(final String str) {
		if (!StringUtils.hasText(str)) {
			return str;
		}
		final Matcher m = SCRIPT_PATTERN.matcher(str);
		int i = 0;
		final StringBuilder sb = new StringBuilder();
		while (m.find()) {
			final int s = m.start();
			final int e = m.end();
			sb.append(str.substring(i, s));
			sb.append(HtmlEncoder.text(str.substring(s, e)));
			i = e;
		}
		if (i == 0) {
			return str;
		}
		return sb.append(str.substring(i)).toString();
	}
}

package net.simpleframework.common.web.html;

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
}

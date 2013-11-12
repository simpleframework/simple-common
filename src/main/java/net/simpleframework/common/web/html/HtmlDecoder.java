package net.simpleframework.common.web.html;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HtmlDecoder {

	private static final Map<String, Character> ENTITY_MAP;

	static {
		final Properties entityReferences = new Properties();

		final InputStream is = HtmlDecoder.class.getResourceAsStream(HtmlDecoder.class
				.getSimpleName() + ".properties");
		if (is == null) {
			throw new IllegalStateException("Entity reference file missing");
		}

		try {
			entityReferences.load(is);
		} catch (final IOException ioex) {
			throw new IllegalStateException(ioex.getMessage());
		} finally {
			try {
				is.close();
			} catch (final IOException e) {
			}
		}

		ENTITY_MAP = new HashMap<String, Character>(entityReferences.size());

		final Enumeration<?> keys = entityReferences.propertyNames();
		while (keys.hasMoreElements()) {
			final String name = (String) keys.nextElement();
			final String hex = entityReferences.getProperty(name);
			final int value = Integer.parseInt(hex, 16);
			ENTITY_MAP.put(name, Character.valueOf((char) value));
		}
	}

	public static String decode(final String html) {
		int ndx = html.indexOf('&');
		if (ndx == -1) {
			return html;
		}

		final StringBuilder result = new StringBuilder(html.length());

		int lastIndex = 0;
		final int len = html.length();
		mainloop: while (ndx != -1) {
			result.append(html.substring(lastIndex, ndx));

			lastIndex = ndx;
			while (html.charAt(lastIndex) != ';') {
				lastIndex++;
				if (lastIndex == len) {
					lastIndex = ndx;
					break mainloop;
				}
			}

			if (html.charAt(ndx + 1) == '#') {
				// decimal/hex
				final char c = html.charAt(ndx + 2);
				int radix;
				if ((c == 'x') || (c == 'X')) {
					radix = 16;
					ndx += 3;
				} else {
					radix = 10;
					ndx += 2;
				}

				final String number = html.substring(ndx, lastIndex);
				final int i = Integer.parseInt(number, radix);
				result.append((char) i);
				lastIndex++;
			} else {
				// token
				final String encodeToken = html.substring(ndx + 1, lastIndex);
				final Character replacement = ENTITY_MAP.get(encodeToken);
				if (replacement == null) {
					result.append(encodeToken);
				} else {
					result.append(replacement.charValue());
					lastIndex++;
				}
			}
			ndx = html.indexOf('&', lastIndex);
		}
		result.append(html.substring(lastIndex));
		return result.toString();
	}
}
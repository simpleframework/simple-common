package net.simpleframework.lib.org.jsoup.internal;

import java.util.Locale;

public class Normalizer {
	public static String lowerCase(final String input) {
		return input.toLowerCase(Locale.ENGLISH);
	}

	public static String normalize(final String input) {
		return lowerCase(input).trim();
	}
}

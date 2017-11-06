package net.simpleframework.lib.org.jsoup.parser;

import static net.simpleframework.lib.org.jsoup.internal.Normalizer.lowerCase;

import net.simpleframework.lib.org.jsoup.nodes.Attributes;

/**
 * Controls parser settings, to optionally preserve tag and/or attribute name
 * case.
 */
public class ParseSettings {
	/**
	 * HTML default settings: both tag and attribute names are lower-cased during
	 * parsing.
	 */
	public static final ParseSettings htmlDefault;
	/**
	 * Preserve both tag and attribute case.
	 */
	public static final ParseSettings preserveCase;

	static {
		htmlDefault = new ParseSettings(false, false);
		preserveCase = new ParseSettings(true, true);
	}

	private final boolean preserveTagCase;
	private final boolean preserveAttributeCase;

	/**
	 * Define parse settings.
	 * 
	 * @param tag
	 *        preserve tag case?
	 * @param attribute
	 *        preserve attribute name case?
	 */
	public ParseSettings(final boolean tag, final boolean attribute) {
		preserveTagCase = tag;
		preserveAttributeCase = attribute;
	}

	String normalizeTag(String name) {
		name = name.trim();
		if (!preserveTagCase) {
			name = lowerCase(name);
		}
		return name;
	}

	String normalizeAttribute(String name) {
		name = name.trim();
		if (!preserveAttributeCase) {
			name = lowerCase(name);
		}
		return name;
	}

	Attributes normalizeAttributes(final Attributes attributes) {
		if (!preserveAttributeCase) {
			attributes.normalize();
		}
		return attributes;
	}
}

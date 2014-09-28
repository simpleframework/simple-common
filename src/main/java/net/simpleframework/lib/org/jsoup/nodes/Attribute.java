package net.simpleframework.lib.org.jsoup.nodes;

import java.util.Arrays;
import java.util.Map;

import net.simpleframework.lib.org.jsoup.helper.Validate;

/**
 * A single key + value attribute. Keys are trimmed and normalised to
 * lower-case.
 * 
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class Attribute implements Map.Entry<String, String>, Cloneable {
	private static final String[] booleanAttributes = { "allowfullscreen", "async", "autofocus",
			"checked", "compact", "declare", "default", "defer", "disabled", "formnovalidate",
			"hidden", "inert", "ismap", "itemscope", "multiple", "muted", "nohref", "noresize",
			"noshade", "novalidate", "nowrap", "open", "readonly", "required", "reversed", "seamless",
			"selected", "sortable", "truespeed", "typemustmatch" };

	private String key;
	private String value;

	/**
	 * Create a new attribute from unencoded (raw) key and value.
	 * 
	 * @param key
	 *        attribute key
	 * @param value
	 *        attribute value
	 * @see #createFromEncoded
	 */
	public Attribute(final String key, final String value) {
		Validate.notEmpty(key);
		Validate.notNull(value);
		this.key = key.trim().toLowerCase();
		this.value = value;
	}

	/**
	 * Get the attribute key.
	 * 
	 * @return the attribute key
	 */
	@Override
	public String getKey() {
		return key;
	}

	/**
	 * Set the attribute key. Gets normalised as per the constructor method.
	 * 
	 * @param key
	 *        the new key; must not be null
	 */
	public void setKey(final String key) {
		Validate.notEmpty(key);
		this.key = key.trim().toLowerCase();
	}

	/**
	 * Get the attribute value.
	 * 
	 * @return the attribute value
	 */
	@Override
	public String getValue() {
		return value;
	}

	/**
	 * Set the attribute value.
	 * 
	 * @param value
	 *        the new attribute value; must not be null
	 */
	@Override
	public String setValue(final String value) {
		Validate.notNull(value);
		final String old = this.value;
		this.value = value;
		return old;
	}

	/**
	 * Get the HTML representation of this attribute; e.g.
	 * {@code href="index.html"}.
	 * 
	 * @return HTML
	 */
	public String html() {
		final StringBuilder accum = new StringBuilder();
		html(accum, (new Document("")).outputSettings());
		return accum.toString();
	}

	protected void html(final StringBuilder accum, final Document.OutputSettings out) {
		accum.append(key);
		if (!shouldCollapseAttribute(out)) {
			accum.append("=\"");
			Entities.escape(accum, value, out, true, false, false);
			accum.append('"');
		}
	}

	/**
	 * Get the string representation of this attribute, implemented as
	 * {@link #html()}.
	 * 
	 * @return string
	 */
	@Override
	public String toString() {
		return html();
	}

	/**
	 * Create a new Attribute from an unencoded key and a HTML attribute encoded
	 * value.
	 * 
	 * @param unencodedKey
	 *        assumes the key is not encoded, as can be only run of simple \w
	 *        chars.
	 * @param encodedValue
	 *        HTML attribute encoded value
	 * @return attribute
	 */
	public static Attribute createFromEncoded(final String unencodedKey, final String encodedValue) {
		final String value = Entities.unescape(encodedValue, true);
		return new Attribute(unencodedKey, value);
	}

	protected boolean isDataAttribute() {
		return key.startsWith(Attributes.dataPrefix) && key.length() > Attributes.dataPrefix.length();
	}

	/**
	 * Collapsible if it's a boolean attribute and value is empty or same as name
	 */
	protected final boolean shouldCollapseAttribute(final Document.OutputSettings out) {
		return ("".equals(value) || value.equalsIgnoreCase(key))
				&& out.syntax() == Document.OutputSettings.Syntax.html
				&& Arrays.binarySearch(booleanAttributes, key) >= 0;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Attribute)) {
			return false;
		}

		final Attribute attribute = (Attribute) o;

		if (key != null ? !key.equals(attribute.key) : attribute.key != null) {
			return false;
		}
		if (value != null ? !value.equals(attribute.value) : attribute.value != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = key != null ? key.hashCode() : 0;
		result = 31 * result + (value != null ? value.hashCode() : 0);
		return result;
	}

	@Override
	public Attribute clone() {
		try {
			return (Attribute) super.clone(); // only fields are immutable strings
															// key and value, so no more deep
															// copy required
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}

package net.simpleframework.lib.org.jsoup.nodes;

import java.util.Collections;
import java.util.List;

import net.simpleframework.lib.org.jsoup.helper.Validate;

abstract class LeafNode extends Node {
	private static final List<Node> EmptyNodes = Collections.emptyList();

	Object value; // either a string value, or an attribute map (in the rare case
						// multiple attributes are set)

	@Override
	protected final boolean hasAttributes() {
		return value instanceof Attributes;
	}

	@Override
	public final Attributes attributes() {
		ensureAttributes();
		return (Attributes) value;
	}

	private void ensureAttributes() {
		if (!hasAttributes()) {
			final Object coreValue = value;
			final Attributes attributes = new Attributes();
			value = attributes;
			if (coreValue != null) {
				attributes.put(nodeName(), (String) coreValue);
			}
		}
	}

	String coreValue() {
		return attr(nodeName());
	}

	void coreValue(final String value) {
		attr(nodeName(), value);
	}

	@Override
	public String attr(final String key) {
		Validate.notNull(key);
		if (!hasAttributes()) {
			return key.equals(nodeName()) ? (String) value : EmptyString;
		}
		return super.attr(key);
	}

	@Override
	public Node attr(final String key, final String value) {
		if (!hasAttributes() && key.equals(nodeName())) {
			this.value = value;
		} else {
			ensureAttributes();
			super.attr(key, value);
		}
		return this;
	}

	@Override
	public boolean hasAttr(final String key) {
		ensureAttributes();
		return super.hasAttr(key);
	}

	@Override
	public Node removeAttr(final String key) {
		ensureAttributes();
		return super.removeAttr(key);
	}

	@Override
	public String absUrl(final String key) {
		ensureAttributes();
		return super.absUrl(key);
	}

	@Override
	public String baseUri() {
		return hasParent() ? parent().baseUri() : "";
	}

	@Override
	protected void doSetBaseUri(final String baseUri) {
		// noop
	}

	@Override
	public int childNodeSize() {
		return 0;
	}

	@Override
	protected List<Node> ensureChildNodes() {
		return EmptyNodes;
	}
}

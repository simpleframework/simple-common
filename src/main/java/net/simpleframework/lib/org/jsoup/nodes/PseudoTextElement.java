package net.simpleframework.lib.org.jsoup.nodes;

import net.simpleframework.lib.org.jsoup.parser.Tag;

/**
 * Represents a {@link TextNode} as an {@link Element}, to enable text nodes to
 * be selected with
 * the {@link net.simpleframework.lib.org.jsoup.select.Selector}
 * {@code :matchText} syntax.
 */
public class PseudoTextElement extends Element {

	public PseudoTextElement(final Tag tag, final String baseUri, final Attributes attributes) {
		super(tag, baseUri, attributes);
	}

	@Override
	void outerHtmlHead(final Appendable accum, final int depth, final Document.OutputSettings out) {
	}

	@Override
	void outerHtmlTail(final Appendable accum, final int depth, final Document.OutputSettings out) {
	}
}

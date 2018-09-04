package net.simpleframework.lib.org.jsoup.nodes;

import java.io.IOException;

import net.simpleframework.lib.org.jsoup.UncheckedIOException;

/**
 * A Character Data node, to support CDATA sections.
 */
public class CDataNode extends TextNode {
	public CDataNode(final String text) {
		super(text);
	}

	@Override
	public String nodeName() {
		return "#cdata";
	}

	/**
	 * Get the unencoded, <b>non-normalized</b> text content of this CDataNode.
	 * 
	 * @return unencoded, non-normalized text
	 */
	@Override
	public String text() {
		return getWholeText();
	}

	@Override
	void outerHtmlHead(final Appendable accum, final int depth, final Document.OutputSettings out)
			throws IOException {
		accum.append("<![CDATA[").append(getWholeText());
	}

	@Override
	void outerHtmlTail(final Appendable accum, final int depth, final Document.OutputSettings out) {
		try {
			accum.append("]]>");
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}

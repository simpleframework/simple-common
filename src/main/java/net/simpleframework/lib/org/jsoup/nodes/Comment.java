package net.simpleframework.lib.org.jsoup.nodes;

import java.io.IOException;

/**
 * A comment node.
 * 
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class Comment extends LeafNode {
	private static final String COMMENT_KEY = "comment";

	/**
	 * Create a new comment node.
	 * 
	 * @param data
	 *        The contents of the comment
	 */
	public Comment(final String data) {
		value = data;
	}

	/**
	 * Create a new comment node.
	 * 
	 * @param data
	 *        The contents of the comment
	 * @param baseUri
	 *        base URI not used. This is a leaf node.
	 * @deprecated
	 */
	@Deprecated
	public Comment(final String data, final String baseUri) {
		this(data);
	}

	@Override
	public String nodeName() {
		return "#comment";
	}

	/**
	 * Get the contents of the comment.
	 * 
	 * @return comment content
	 */
	public String getData() {
		return coreValue();
	}

	@Override
	void outerHtmlHead(final Appendable accum, final int depth, final Document.OutputSettings out)
			throws IOException {
		if (out.prettyPrint()) {
			indent(accum, depth, out);
		}
		accum.append("<!--").append(getData()).append("-->");
	}

	@Override
	void outerHtmlTail(final Appendable accum, final int depth, final Document.OutputSettings out) {
	}

	@Override
	public String toString() {
		return outerHtml();
	}
}

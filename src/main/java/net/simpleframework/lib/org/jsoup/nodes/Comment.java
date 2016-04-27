package net.simpleframework.lib.org.jsoup.nodes;

import java.io.IOException;

/**
 * A comment node.
 * 
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class Comment extends Node {
	private static final String COMMENT_KEY = "comment";

	/**
	 * Create a new comment node.
	 * 
	 * @param data
	 *        The contents of the comment
	 * @param baseUri
	 *        base URI
	 */
	public Comment(final String data, final String baseUri) {
		super(baseUri);
		attributes.put(COMMENT_KEY, data);
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
		return attributes.get(COMMENT_KEY);
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

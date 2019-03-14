package net.simpleframework.lib.org.jsoup.nodes;

import java.io.IOException;

import net.simpleframework.lib.org.jsoup.Jsoup;
import net.simpleframework.lib.org.jsoup.parser.Parser;

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

	/**
	 * Check if this comment looks like an XML Declaration.
	 * 
	 * @return true if it looks like, maybe, it's an XML Declaration.
	 */
	public boolean isXmlDeclaration() {
		final String data = getData();
		return (data.length() > 1 && (data.startsWith("!") || data.startsWith("?")));
	}

	/**
	 * Attempt to cast this comment to an XML Declaration note.
	 * 
	 * @return an XML declaration if it could be parsed as one, null otherwise.
	 */
	public XmlDeclaration asXmlDeclaration() {
		final String data = getData();
		final Document doc = Jsoup.parse("<" + data.substring(1, data.length() - 1) + ">", baseUri(),
				Parser.xmlParser());
		XmlDeclaration decl = null;
		if (doc.children().size() > 0) {
			final Element el = doc.child(0);
			decl = new XmlDeclaration(NodeUtils.parser(doc).settings().normalizeTag(el.tagName()),
					data.startsWith("!"));
			decl.attributes().addAll(el.attributes());
		}
		return decl;
	}
}

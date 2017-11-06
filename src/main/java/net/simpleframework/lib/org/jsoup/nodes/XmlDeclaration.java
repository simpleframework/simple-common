package net.simpleframework.lib.org.jsoup.nodes;

import java.io.IOException;

import net.simpleframework.lib.org.jsoup.SerializationException;
import net.simpleframework.lib.org.jsoup.helper.Validate;

/**
 * An XML Declaration.
 */
public class XmlDeclaration extends LeafNode {
	// todo this impl isn't really right, the data shouldn't be attributes, just
	// a run of text after the name
	private final boolean isProcessingInstruction; // <! if true, <? if false,
																	// declaration (and last data
																	// char should be ?)

	/**
	 * Create a new XML declaration
	 * 
	 * @param name
	 *        of declaration
	 * @param isProcessingInstruction
	 *        is processing instruction
	 */
	public XmlDeclaration(final String name, final boolean isProcessingInstruction) {
		Validate.notNull(name);
		value = name;
		this.isProcessingInstruction = isProcessingInstruction;
	}

	/**
	 * Create a new XML declaration
	 * 
	 * @param name
	 *        of declaration
	 * @param baseUri
	 *        Leaf Nodes don't have base URIs; they inherit from their Element
	 * @param isProcessingInstruction
	 *        is processing instruction
	 * @see XmlDeclaration#XmlDeclaration(String, boolean)
	 * @deprecated
	 */
	@Deprecated
	public XmlDeclaration(final String name, final String baseUri,
			final boolean isProcessingInstruction) {
		this(name, isProcessingInstruction);
	}

	@Override
	public String nodeName() {
		return "#declaration";
	}

	/**
	 * Get the name of this declaration.
	 * 
	 * @return name of this declaration.
	 */
	public String name() {
		return coreValue();
	}

	/**
	 * Get the unencoded XML declaration.
	 * 
	 * @return XML declaration
	 */
	public String getWholeDeclaration() {
		final StringBuilder sb = new StringBuilder();
		try {
			getWholeDeclaration(sb, new Document.OutputSettings());
		} catch (final IOException e) {
			throw new SerializationException(e);
		}
		return sb.toString().trim();
	}

	private void getWholeDeclaration(final Appendable accum, final Document.OutputSettings out)
			throws IOException {
		for (final Attribute attribute : attributes()) {
			if (!attribute.getKey().equals(nodeName())) { // skips coreValue (name)
				accum.append(' ');
				attribute.html(accum, out);
			}
		}
	}

	@Override
	void outerHtmlHead(final Appendable accum, final int depth, final Document.OutputSettings out)
			throws IOException {
		accum.append("<").append(isProcessingInstruction ? "!" : "?").append(coreValue());
		getWholeDeclaration(accum, out);
		accum.append(isProcessingInstruction ? "!" : "?").append(">");
	}

	@Override
	void outerHtmlTail(final Appendable accum, final int depth, final Document.OutputSettings out) {
	}

	@Override
	public String toString() {
		return outerHtml();
	}
}

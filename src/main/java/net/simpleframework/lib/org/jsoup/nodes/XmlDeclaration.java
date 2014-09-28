package net.simpleframework.lib.org.jsoup.nodes;

/**
 * An XML Declaration.
 * 
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class XmlDeclaration extends Node {
	private static final String DECL_KEY = "declaration";
	private final boolean isProcessingInstruction; // <! if true, <? if false,
																	// declaration (and last data
																	// char should be ?)

	/**
	 * Create a new XML declaration
	 * 
	 * @param data
	 *        data
	 * @param baseUri
	 *        base uri
	 * @param isProcessingInstruction
	 *        is processing instruction
	 */
	public XmlDeclaration(final String data, final String baseUri,
			final boolean isProcessingInstruction) {
		super(baseUri);
		attributes.put(DECL_KEY, data);
		this.isProcessingInstruction = isProcessingInstruction;
	}

	@Override
	public String nodeName() {
		return "#declaration";
	}

	/**
	 * Get the unencoded XML declaration.
	 * 
	 * @return XML declaration
	 */
	public String getWholeDeclaration() {
		return attributes.get(DECL_KEY);
	}

	@Override
	void outerHtmlHead(final StringBuilder accum, final int depth, final Document.OutputSettings out) {
		accum.append("<").append(isProcessingInstruction ? "!" : "?").append(getWholeDeclaration())
				.append(">");
	}

	@Override
	void outerHtmlTail(final StringBuilder accum, final int depth, final Document.OutputSettings out) {
	}

	@Override
	public String toString() {
		return outerHtml();
	}
}

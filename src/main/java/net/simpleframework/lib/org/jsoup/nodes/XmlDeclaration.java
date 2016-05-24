package net.simpleframework.lib.org.jsoup.nodes;

import java.io.IOException;

import net.simpleframework.lib.org.jsoup.helper.Validate;

/**
 * An XML Declaration.
 * 
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class XmlDeclaration extends Node {
	private final String name;
	private final boolean isProcessingInstruction; // <! if true, <? if false,
																	// declaration (and last data
																	// char should be ?)

	/**
	 * Create a new XML declaration
	 * 
	 * @param name
	 *        of declaration
	 * @param baseUri
	 *        base uri
	 * @param isProcessingInstruction
	 *        is processing instruction
	 */
	public XmlDeclaration(final String name, final String baseUri,
			final boolean isProcessingInstruction) {
		super(baseUri);
		Validate.notNull(name);
		this.name = name;
		this.isProcessingInstruction = isProcessingInstruction;
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
		return name;
	}

	/**
	 * Get the unencoded XML declaration.
	 * 
	 * @return XML declaration
	 */
	public String getWholeDeclaration() {
		return attributes.html().trim(); // attr html starts with a " "
	}

	@Override
	void outerHtmlHead(final Appendable accum, final int depth, final Document.OutputSettings out)
			throws IOException {
		accum.append("<").append(isProcessingInstruction ? "!" : "?").append(name);
		attributes.html(accum, out);
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

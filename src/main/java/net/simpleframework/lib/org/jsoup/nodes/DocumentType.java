package net.simpleframework.lib.org.jsoup.nodes;

import net.simpleframework.lib.org.jsoup.helper.StringUtil;
import net.simpleframework.lib.org.jsoup.helper.Validate;

/**
 * A {@code <!DOCTYPE>} node.
 */
public class DocumentType extends Node {
	// todo: quirk mode from publicId and systemId

	/**
	 * Create a new doctype element.
	 * 
	 * @param name
	 *        the doctype's name
	 * @param publicId
	 *        the doctype's public ID
	 * @param systemId
	 *        the doctype's system ID
	 * @param baseUri
	 *        the doctype's base URI
	 */
	public DocumentType(final String name, final String publicId, final String systemId,
			final String baseUri) {
		super(baseUri);

		Validate.notEmpty(name);
		attr("name", name);
		attr("publicId", publicId);
		attr("systemId", systemId);
	}

	@Override
	public String nodeName() {
		return "#doctype";
	}

	@Override
	void outerHtmlHead(final StringBuilder accum, final int depth, final Document.OutputSettings out) {
		accum.append("<!DOCTYPE ").append(attr("name"));
		if (!StringUtil.isBlank(attr("publicId"))) {
			accum.append(" PUBLIC \"").append(attr("publicId")).append('"');
		}
		if (!StringUtil.isBlank(attr("systemId"))) {
			accum.append(" \"").append(attr("systemId")).append('"');
		}
		accum.append('>');
	}

	@Override
	void outerHtmlTail(final StringBuilder accum, final int depth, final Document.OutputSettings out) {
	}
}

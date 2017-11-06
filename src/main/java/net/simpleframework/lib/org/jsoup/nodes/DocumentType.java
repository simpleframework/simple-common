package net.simpleframework.lib.org.jsoup.nodes;

import java.io.IOException;

import net.simpleframework.lib.org.jsoup.helper.StringUtil;
import net.simpleframework.lib.org.jsoup.helper.Validate;
import net.simpleframework.lib.org.jsoup.nodes.Document.OutputSettings.Syntax;

/**
 * A {@code <!DOCTYPE>} node.
 */
public class DocumentType extends LeafNode {
	// todo needs a bit of a chunky cleanup. this level of detail isn't needed
	public static final String PUBLIC_KEY = "PUBLIC";
	public static final String SYSTEM_KEY = "SYSTEM";
	private static final String NAME = "name";
	private static final String PUB_SYS_KEY = "pubSysKey"; // PUBLIC or SYSTEM
	private static final String PUBLIC_ID = "publicId";
	private static final String SYSTEM_ID = "systemId";
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
	 */
	public DocumentType(final String name, final String publicId, final String systemId) {
		Validate.notNull(name);
		Validate.notNull(publicId);
		Validate.notNull(systemId);
		attr(NAME, name);
		attr(PUBLIC_ID, publicId);
		if (has(PUBLIC_ID)) {
			attr(PUB_SYS_KEY, PUBLIC_KEY);
		}
		attr(SYSTEM_ID, systemId);
	}

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
	 *        unused
	 * @deprecated
	 */
	@Deprecated
	public DocumentType(final String name, final String publicId, final String systemId,
			final String baseUri) {
		attr(NAME, name);
		attr(PUBLIC_ID, publicId);
		if (has(PUBLIC_ID)) {
			attr(PUB_SYS_KEY, PUBLIC_KEY);
		}
		attr(SYSTEM_ID, systemId);
	}

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
	 *        unused
	 * @deprecated
	 */
	@Deprecated
	public DocumentType(final String name, final String pubSysKey, final String publicId,
			final String systemId, final String baseUri) {
		attr(NAME, name);
		if (pubSysKey != null) {
			attr(PUB_SYS_KEY, pubSysKey);
		}
		attr(PUBLIC_ID, publicId);
		attr(SYSTEM_ID, systemId);
	}

	public void setPubSysKey(final String value) {
		if (value != null) {
			attr(PUB_SYS_KEY, value);
		}
	}

	@Override
	public String nodeName() {
		return "#doctype";
	}

	@Override
	void outerHtmlHead(final Appendable accum, final int depth, final Document.OutputSettings out)
			throws IOException {
		if (out.syntax() == Syntax.html && !has(PUBLIC_ID) && !has(SYSTEM_ID)) {
			// looks like a html5 doctype, go lowercase for aesthetics
			accum.append("<!doctype");
		} else {
			accum.append("<!DOCTYPE");
		}
		if (has(NAME)) {
			accum.append(" ").append(attr(NAME));
		}
		if (has(PUB_SYS_KEY)) {
			accum.append(" ").append(attr(PUB_SYS_KEY));
		}
		if (has(PUBLIC_ID)) {
			accum.append(" \"").append(attr(PUBLIC_ID)).append('"');
		}
		if (has(SYSTEM_ID)) {
			accum.append(" \"").append(attr(SYSTEM_ID)).append('"');
		}
		accum.append('>');
	}

	@Override
	void outerHtmlTail(final Appendable accum, final int depth, final Document.OutputSettings out) {
	}

	private boolean has(final String attribute) {
		return !StringUtil.isBlank(attr(attribute));
	}
}

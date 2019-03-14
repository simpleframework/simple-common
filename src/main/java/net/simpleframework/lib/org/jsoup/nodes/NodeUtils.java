package net.simpleframework.lib.org.jsoup.nodes;

import net.simpleframework.lib.org.jsoup.parser.HtmlTreeBuilder;
import net.simpleframework.lib.org.jsoup.parser.Parser;

/**
 * Internal helpers for Nodes, to keep the actual node APIs relatively clean. A
 * jsoup internal class, so don't use it as
 * there is no contract API).
 */
final class NodeUtils {
	/**
	 * Get the output setting for this node, or if this node has no document (or
	 * parent), retrieve the default output
	 * settings
	 */
	static Document.OutputSettings outputSettings(final Node node) {
		final Document owner = node.ownerDocument();
		return owner != null ? owner.outputSettings() : (new Document("")).outputSettings();
	}

	/**
	 * Get the parser that was used to make this node, or the default HTML parser
	 * if it has no parent.
	 */
	static Parser parser(final Node node) {
		final Document doc = node.ownerDocument();
		return doc != null && doc.parser() != null ? doc.parser() : new Parser(new HtmlTreeBuilder());
	}
}

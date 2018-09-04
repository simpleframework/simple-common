package net.simpleframework.lib.org.jsoup.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.nodes.Node;

/**
 * Parses HTML into a {@link net.simpleframework.lib.org.jsoup.nodes.Document}.
 * Generally best to use one of the more convenient parse methods
 * in {@link net.simpleframework.lib.org.jsoup.Jsoup}.
 */
public class Parser {
	private static final int DEFAULT_MAX_ERRORS = 0; // by default, error
																		// tracking is disabled.

	private TreeBuilder treeBuilder;
	private int maxErrors = DEFAULT_MAX_ERRORS;
	private ParseErrorList errors;
	private ParseSettings settings;

	/**
	 * Create a new Parser, using the specified TreeBuilder
	 * 
	 * @param treeBuilder
	 *        TreeBuilder to use to parse input into Documents.
	 */
	public Parser(final TreeBuilder treeBuilder) {
		this.treeBuilder = treeBuilder;
		settings = treeBuilder.defaultSettings();
	}

	public Document parseInput(final String html, final String baseUri) {
		errors = isTrackErrors() ? ParseErrorList.tracking(maxErrors) : ParseErrorList.noTracking();
		return treeBuilder.parse(new StringReader(html), baseUri, errors, settings);
	}

	public Document parseInput(final Reader inputHtml, final String baseUri) {
		errors = isTrackErrors() ? ParseErrorList.tracking(maxErrors) : ParseErrorList.noTracking();
		return treeBuilder.parse(inputHtml, baseUri, errors, settings);
	}

	// gets & sets
	/**
	 * Get the TreeBuilder currently in use.
	 * 
	 * @return current TreeBuilder.
	 */
	public TreeBuilder getTreeBuilder() {
		return treeBuilder;
	}

	/**
	 * Update the TreeBuilder used when parsing content.
	 * 
	 * @param treeBuilder
	 *        current TreeBuilder
	 * @return this, for chaining
	 */
	public Parser setTreeBuilder(final TreeBuilder treeBuilder) {
		this.treeBuilder = treeBuilder;
		return this;
	}

	/**
	 * Check if parse error tracking is enabled.
	 * 
	 * @return current track error state.
	 */
	public boolean isTrackErrors() {
		return maxErrors > 0;
	}

	/**
	 * Enable or disable parse error tracking for the next parse.
	 * 
	 * @param maxErrors
	 *        the maximum number of errors to track. Set to 0 to disable.
	 * @return this, for chaining
	 */
	public Parser setTrackErrors(final int maxErrors) {
		this.maxErrors = maxErrors;
		return this;
	}

	/**
	 * Retrieve the parse errors, if any, from the last parse.
	 * 
	 * @return list of parse errors, up to the size of the maximum errors
	 *         tracked.
	 */
	public List<ParseError> getErrors() {
		return errors;
	}

	public Parser settings(final ParseSettings settings) {
		this.settings = settings;
		return this;
	}

	public ParseSettings settings() {
		return settings;
	}

	// static parse functions below
	/**
	 * Parse HTML into a Document.
	 *
	 * @param html
	 *        HTML to parse
	 * @param baseUri
	 *        base URI of document (i.e. original fetch location), for resolving
	 *        relative URLs.
	 *
	 * @return parsed Document
	 */
	public static Document parse(final String html, final String baseUri) {
		final TreeBuilder treeBuilder = new HtmlTreeBuilder();
		return treeBuilder.parse(new StringReader(html), baseUri, ParseErrorList.noTracking(),
				treeBuilder.defaultSettings());
	}

	/**
	 * Parse a fragment of HTML into a list of nodes. The context element, if
	 * supplied, supplies parsing context.
	 *
	 * @param fragmentHtml
	 *        the fragment of HTML to parse
	 * @param context
	 *        (optional) the element that this HTML fragment is being parsed for
	 *        (i.e. for inner HTML). This
	 *        provides stack context (for implicit element creation).
	 * @param baseUri
	 *        base URI of document (i.e. original fetch location), for resolving
	 *        relative URLs.
	 *
	 * @return list of nodes parsed from the input HTML. Note that the context
	 *         element, if supplied, is not modified.
	 */
	public static List<Node> parseFragment(final String fragmentHtml, final Element context,
			final String baseUri) {
		final HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
		return treeBuilder.parseFragment(fragmentHtml, context, baseUri, ParseErrorList.noTracking(),
				treeBuilder.defaultSettings());
	}

	/**
	 * Parse a fragment of HTML into a list of nodes. The context element, if
	 * supplied, supplies parsing context.
	 *
	 * @param fragmentHtml
	 *        the fragment of HTML to parse
	 * @param context
	 *        (optional) the element that this HTML fragment is being parsed for
	 *        (i.e. for inner HTML). This
	 *        provides stack context (for implicit element creation).
	 * @param baseUri
	 *        base URI of document (i.e. original fetch location), for resolving
	 *        relative URLs.
	 * @param errorList
	 *        list to add errors to
	 *
	 * @return list of nodes parsed from the input HTML. Note that the context
	 *         element, if supplied, is not modified.
	 */
	public static List<Node> parseFragment(final String fragmentHtml, final Element context,
			final String baseUri, final ParseErrorList errorList) {
		final HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
		return treeBuilder.parseFragment(fragmentHtml, context, baseUri, errorList,
				treeBuilder.defaultSettings());
	}

	/**
	 * Parse a fragment of XML into a list of nodes.
	 *
	 * @param fragmentXml
	 *        the fragment of XML to parse
	 * @param baseUri
	 *        base URI of document (i.e. original fetch location), for resolving
	 *        relative URLs.
	 * @return list of nodes parsed from the input XML.
	 */
	public static List<Node> parseXmlFragment(final String fragmentXml, final String baseUri) {
		final XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
		return treeBuilder.parseFragment(fragmentXml, baseUri, ParseErrorList.noTracking(),
				treeBuilder.defaultSettings());
	}

	/**
	 * Parse a fragment of HTML into the {@code body} of a Document.
	 *
	 * @param bodyHtml
	 *        fragment of HTML
	 * @param baseUri
	 *        base URI of document (i.e. original fetch location), for resolving
	 *        relative URLs.
	 *
	 * @return Document, with empty head, and HTML parsed into body
	 */
	public static Document parseBodyFragment(final String bodyHtml, final String baseUri) {
		final Document doc = Document.createShell(baseUri);
		final Element body = doc.body();
		final List<Node> nodeList = parseFragment(bodyHtml, body, baseUri);
		final Node[] nodes = nodeList.toArray(new Node[nodeList.size()]); // the
																								// node
		// list gets
		// modified
		// when
		// re-parented
		for (int i = nodes.length - 1; i > 0; i--) {
			nodes[i].remove();
		}
		for (final Node node : nodes) {
			body.appendChild(node);
		}
		return doc;
	}

	/**
	 * Utility method to unescape HTML entities from a string
	 * 
	 * @param string
	 *        HTML escaped string
	 * @param inAttribute
	 *        if the string is to be escaped in strict mode (as attributes are)
	 * @return an unescaped string
	 */
	public static String unescapeEntities(final String string, final boolean inAttribute) {
		final Tokeniser tokeniser = new Tokeniser(new CharacterReader(string),
				ParseErrorList.noTracking());
		return tokeniser.unescapeEntities(inAttribute);
	}

	/**
	 * @param bodyHtml
	 *        HTML to parse
	 * @param baseUri
	 *        baseUri base URI of document (i.e. original fetch location), for
	 *        resolving relative URLs.
	 *
	 * @return parsed Document
	 * @deprecated Use {@link #parseBodyFragment} or {@link #parseFragment}
	 *             instead.
	 */
	@Deprecated
	public static Document parseBodyFragmentRelaxed(final String bodyHtml, final String baseUri) {
		return parse(bodyHtml, baseUri);
	}

	// builders

	/**
	 * Create a new HTML parser. This parser treats input as HTML5, and enforces
	 * the creation of a normalised document,
	 * based on a knowledge of the semantics of the incoming tags.
	 * 
	 * @return a new HTML parser.
	 */
	public static Parser htmlParser() {
		return new Parser(new HtmlTreeBuilder());
	}

	/**
	 * Create a new XML parser. This parser assumes no knowledge of the incoming
	 * tags and does not treat it as HTML,
	 * rather creates a simple tree directly from the input.
	 * 
	 * @return a new simple XML parser.
	 */
	public static Parser xmlParser() {
		return new Parser(new XmlTreeBuilder());
	}
}

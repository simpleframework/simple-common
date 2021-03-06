package net.simpleframework.lib.org.jsoup.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import net.simpleframework.lib.org.jsoup.helper.Validate;
import net.simpleframework.lib.org.jsoup.nodes.Attributes;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.nodes.Node;

/**
 * @author Jonathan Hedley
 */
abstract class TreeBuilder {
	protected Parser parser;
	CharacterReader reader;
	Tokeniser tokeniser;
	protected Document doc; // current doc we are building into
	protected ArrayList<Element> stack; // the stack of open elements
	protected String baseUri; // current base uri, for creating new elements
	protected Token currentToken; // currentToken is used only for error
											// tracking.
	protected ParseSettings settings;

	private final Token.StartTag start = new Token.StartTag(); // start tag to
																					// process
	private final Token.EndTag end = new Token.EndTag();

	abstract ParseSettings defaultSettings();

	protected void initialiseParse(final Reader input, final String baseUri, final Parser parser) {
		Validate.notNull(input, "String input must not be null");
		Validate.notNull(baseUri, "BaseURI must not be null");

		doc = new Document(baseUri);
		doc.parser(parser);
		this.parser = parser;
		settings = parser.settings();
		reader = new CharacterReader(input);
		currentToken = null;
		tokeniser = new Tokeniser(reader, parser.getErrors());
		stack = new ArrayList<>(32);
		this.baseUri = baseUri;
	}

	Document parse(final Reader input, final String baseUri, final Parser parser) {
		initialiseParse(input, baseUri, parser);
		runParser();

		// tidy up - as the Parser and Treebuilder are retained in document for
		// settings / fragments
		reader.close();
		reader = null;
		tokeniser = null;
		stack = null;

		return doc;
	}

	abstract List<Node> parseFragment(String inputFragment, Element context, String baseUri,
			Parser parser);

	protected void runParser() {
		final Tokeniser tokeniser = this.tokeniser;
		final Token.TokenType eof = Token.TokenType.EOF;

		while (true) {
			final Token token = tokeniser.read();
			process(token);
			token.reset();

			if (token.type == eof) {
				break;
			}
		}
	}

	protected abstract boolean process(Token token);

	protected boolean processStartTag(final String name) {
		final Token.StartTag start = this.start;
		if (currentToken == start) { // don't recycle an in-use token
			return process(new Token.StartTag().name(name));
		}
		return process(start.reset().name(name));
	}

	public boolean processStartTag(final String name, final Attributes attrs) {
		final Token.StartTag start = this.start;
		if (currentToken == start) { // don't recycle an in-use token
			return process(new Token.StartTag().nameAttr(name, attrs));
		}
		start.reset();
		start.nameAttr(name, attrs);
		return process(start);
	}

	protected boolean processEndTag(final String name) {
		if (currentToken == end) { // don't recycle an in-use token
			return process(new Token.EndTag().name(name));
		}
		return process(end.reset().name(name));
	}

	protected Element currentElement() {
		final int size = stack.size();
		return size > 0 ? stack.get(size - 1) : null;
	}

	/**
	 * If the parser is tracking errors, and an error at the current position.
	 * 
	 * @param msg
	 *        error message
	 */
	protected void error(final String msg) {
		final ParseErrorList errors = parser.getErrors();
		if (errors.canAddError()) {
			errors.add(new ParseError(reader.pos(), msg));
		}
	}
}

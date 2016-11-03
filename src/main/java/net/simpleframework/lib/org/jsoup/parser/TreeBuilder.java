package net.simpleframework.lib.org.jsoup.parser;

import java.util.ArrayList;

import net.simpleframework.lib.org.jsoup.helper.Validate;
import net.simpleframework.lib.org.jsoup.nodes.Attributes;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.nodes.Element;

/**
 * @author Jonathan Hedley
 */
abstract class TreeBuilder {
	CharacterReader reader;
	Tokeniser tokeniser;
	protected Document doc; // current doc we are building into
	protected ArrayList<Element> stack; // the stack of open elements
	protected String baseUri; // current base uri, for creating new elements
	protected Token currentToken; // currentToken is used only for error
											// tracking.
	protected ParseErrorList errors; // null when not tracking errors
	protected ParseSettings settings;

	private final Token.StartTag start = new Token.StartTag(); // start tag to
																					// process
	private final Token.EndTag end = new Token.EndTag();

	abstract ParseSettings defaultSettings();

	protected void initialiseParse(final String input, final String baseUri,
			final ParseErrorList errors, final ParseSettings settings) {
		Validate.notNull(input, "String input must not be null");
		Validate.notNull(baseUri, "BaseURI must not be null");

		doc = new Document(baseUri);
		this.settings = settings;
		reader = new CharacterReader(input);
		this.errors = errors;
		tokeniser = new Tokeniser(reader, errors);
		stack = new ArrayList<Element>(32);
		this.baseUri = baseUri;
	}

	Document parse(final String input, final String baseUri, final ParseErrorList errors,
			final ParseSettings settings) {
		initialiseParse(input, baseUri, errors, settings);
		runParser();
		return doc;
	}

	protected void runParser() {
		while (true) {
			final Token token = tokeniser.read();
			process(token);
			token.reset();

			if (token.type == Token.TokenType.EOF) {
				break;
			}
		}
	}

	protected abstract boolean process(Token token);

	protected boolean processStartTag(final String name) {
		if (currentToken == start) { // don't recycle an in-use token
			return process(new Token.StartTag().name(name));
		}
		return process(start.reset().name(name));
	}

	public boolean processStartTag(final String name, final Attributes attrs) {
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
}

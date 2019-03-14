package net.simpleframework.lib.org.jsoup.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import net.simpleframework.lib.org.jsoup.helper.Validate;
import net.simpleframework.lib.org.jsoup.nodes.CDataNode;
import net.simpleframework.lib.org.jsoup.nodes.Comment;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.nodes.DocumentType;
import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.nodes.Node;
import net.simpleframework.lib.org.jsoup.nodes.TextNode;
import net.simpleframework.lib.org.jsoup.nodes.XmlDeclaration;

/**
 * Use the {@code XmlTreeBuilder} when you want to parse XML without any of the
 * HTML DOM rules being applied to the
 * document.
 * <p>
 * Usage example:
 * {@code Document xmlDoc = Jsoup.parse(html, baseUrl, Parser.xmlParser());}
 * </p>
 *
 * @author Jonathan Hedley
 */
public class XmlTreeBuilder extends TreeBuilder {
	@Override
	ParseSettings defaultSettings() {
		return ParseSettings.preserveCase;
	}

	@Override
	protected void initialiseParse(final Reader input, final String baseUri, final Parser parser) {
		super.initialiseParse(input, baseUri, parser);
		stack.add(doc); // place the document onto the stack. differs from
								// HtmlTreeBuilder (not on stack)
		doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
	}

	Document parse(final Reader input, final String baseUri) {
		return parse(input, baseUri, new Parser(this));
	}

	Document parse(final String input, final String baseUri) {
		return parse(new StringReader(input), baseUri, new Parser(this));
	}

	@Override
	protected boolean process(final Token token) {
		// start tag, end tag, doctype, comment, character, eof
		switch (token.type) {
		case StartTag:
			insert(token.asStartTag());
			break;
		case EndTag:
			popStackToClose(token.asEndTag());
			break;
		case Comment:
			insert(token.asComment());
			break;
		case Character:
			insert(token.asCharacter());
			break;
		case Doctype:
			insert(token.asDoctype());
			break;
		case EOF: // could put some normalisation here if desired
			break;
		default:
			Validate.fail("Unexpected token type: " + token.type);
		}
		return true;
	}

	private void insertNode(final Node node) {
		currentElement().appendChild(node);
	}

	Element insert(final Token.StartTag startTag) {
		final Tag tag = Tag.valueOf(startTag.name(), settings);
		// todo: wonder if for xml parsing, should treat all tags as unknown?
		// because it's not html.
		final Element el = new Element(tag, baseUri,
				settings.normalizeAttributes(startTag.attributes));
		insertNode(el);
		if (startTag.isSelfClosing()) {
			if (!tag.isKnownTag()) {
				// for output. see above.
				tag.setSelfClosing();
			}
		} else {
			stack.add(el);
		}
		return el;
	}

	void insert(final Token.Comment commentToken) {
		final Comment comment = new Comment(commentToken.getData());
		Node insert = comment;
		if (commentToken.bogus && comment.isXmlDeclaration()) {
			// xml declarations are emitted as bogus comments (which is right for
			// html, but not xml)
			// so we do a bit of a hack and parse the data as an element to pull
			// the attributes out
			final XmlDeclaration decl = comment.asXmlDeclaration(); // else, we
																						// couldn't
			// parse it as a
			// decl, so leave as
			// a comment
			if (decl != null) {
				insert = decl;
			}
		}
		insertNode(insert);
	}

	void insert(final Token.Character token) {
		final String data = token.getData();
		insertNode(token.isCData() ? new CDataNode(data) : new TextNode(data));
	}

	void insert(final Token.Doctype d) {
		final DocumentType doctypeNode = new DocumentType(settings.normalizeTag(d.getName()),
				d.getPublicIdentifier(), d.getSystemIdentifier());
		doctypeNode.setPubSysKey(d.getPubSysKey());
		insertNode(doctypeNode);
	}

	/**
	 * If the stack contains an element with this tag's name, pop up the stack to
	 * remove the first occurrence. If not
	 * found, skips.
	 *
	 * @param endTag
	 *        tag to close
	 */
	private void popStackToClose(final Token.EndTag endTag) {
		final String elName = settings.normalizeTag(endTag.tagName);
		Element firstFound = null;

		for (int pos = stack.size() - 1; pos >= 0; pos--) {
			final Element next = stack.get(pos);
			if (next.nodeName().equals(elName)) {
				firstFound = next;
				break;
			}
		}
		if (firstFound == null) {
			return; // not found, skip
		}

		for (int pos = stack.size() - 1; pos >= 0; pos--) {
			final Element next = stack.get(pos);
			stack.remove(pos);
			if (next == firstFound) {
				break;
			}
		}
	}

	List<Node> parseFragment(final String inputFragment, final String baseUri, final Parser parser) {
		initialiseParse(new StringReader(inputFragment), baseUri, parser);
		runParser();
		return doc.childNodes();
	}

	@Override
	List<Node> parseFragment(final String inputFragment, final Element context, final String baseUri,
			final Parser parser) {
		return parseFragment(inputFragment, baseUri, parser);
	}
}

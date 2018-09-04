package net.simpleframework.lib.org.jsoup.parser;

import static net.simpleframework.lib.org.jsoup.helper.StringUtil.inSorted;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.simpleframework.lib.org.jsoup.helper.StringUtil;
import net.simpleframework.lib.org.jsoup.helper.Validate;
import net.simpleframework.lib.org.jsoup.nodes.CDataNode;
import net.simpleframework.lib.org.jsoup.nodes.Comment;
import net.simpleframework.lib.org.jsoup.nodes.DataNode;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.nodes.FormElement;
import net.simpleframework.lib.org.jsoup.nodes.Node;
import net.simpleframework.lib.org.jsoup.nodes.TextNode;
import net.simpleframework.lib.org.jsoup.select.Elements;

/**
 * HTML Tree Builder; creates a DOM from Tokens.
 */
public class HtmlTreeBuilder extends TreeBuilder {
	// tag searches. must be sorted, used in inSorted. MUST update
	// HtmlTreeBuilderTest if more arrays are added.
	static final String[] TagsSearchInScope = new String[] { "applet", "caption", "html", "marquee",
			"object", "table", "td", "th" };
	static final String[] TagSearchList = new String[] { "ol", "ul" };
	static final String[] TagSearchButton = new String[] { "button" };
	static final String[] TagSearchTableScope = new String[] { "html", "table" };
	static final String[] TagSearchSelectScope = new String[] { "optgroup", "option" };
	static final String[] TagSearchEndTags = new String[] { "dd", "dt", "li", "optgroup", "option",
			"p", "rp", "rt" };
	static final String[] TagSearchSpecial = new String[] { "address", "applet", "area", "article",
			"aside", "base", "basefont", "bgsound", "blockquote", "body", "br", "button", "caption",
			"center", "col", "colgroup", "command", "dd", "details", "dir", "div", "dl", "dt", "embed",
			"fieldset", "figcaption", "figure", "footer", "form", "frame", "frameset", "h1", "h2",
			"h3", "h4", "h5", "h6", "head", "header", "hgroup", "hr", "html", "iframe", "img", "input",
			"isindex", "li", "link", "listing", "marquee", "menu", "meta", "nav", "noembed",
			"noframes", "noscript", "object", "ol", "p", "param", "plaintext", "pre", "script",
			"section", "select", "style", "summary", "table", "tbody", "td", "textarea", "tfoot", "th",
			"thead", "title", "tr", "ul", "wbr", "xmp" };

	public static final int MaxScopeSearchDepth = 100; // prevents the parser
																		// bogging down in
																		// exceptionally broken
																		// pages

	private HtmlTreeBuilderState state; // the current state
	private HtmlTreeBuilderState originalState; // original / marked state

	private boolean baseUriSetFromDoc;
	private Element headElement; // the current head element
	private FormElement formElement; // the current form element
	private Element contextElement; // fragment parse context -- could be null
												// even if fragment parsing
	private ArrayList<Element> formattingElements; // active (open) formatting
																	// elements
	private List<String> pendingTableCharacters; // chars in table to be shifted
																// out
	private Token.EndTag emptyEnd; // reused empty end tag

	private boolean framesetOk; // if ok to go into frameset
	private boolean fosterInserts; // if next inserts should be fostered
	private boolean fragmentParsing; // if parsing a fragment of html

	@Override
	ParseSettings defaultSettings() {
		return ParseSettings.htmlDefault;
	}

	@Override
	protected void initialiseParse(final Reader input, final String baseUri,
			final ParseErrorList errors, final ParseSettings settings) {
		super.initialiseParse(input, baseUri, errors, settings);

		// this is a bit mucky. todo - probably just create new parser objects to
		// ensure all reset.
		state = HtmlTreeBuilderState.Initial;
		originalState = null;
		baseUriSetFromDoc = false;
		headElement = null;
		formElement = null;
		contextElement = null;
		formattingElements = new ArrayList<>();
		pendingTableCharacters = new ArrayList<>();
		emptyEnd = new Token.EndTag();
		framesetOk = true;
		fosterInserts = false;
		fragmentParsing = false;
	}

	List<Node> parseFragment(final String inputFragment, final Element context, final String baseUri,
			final ParseErrorList errors, final ParseSettings settings) {
		// context may be null
		state = HtmlTreeBuilderState.Initial;
		initialiseParse(new StringReader(inputFragment), baseUri, errors, settings);
		contextElement = context;
		fragmentParsing = true;
		Element root = null;

		if (context != null) {
			if (context.ownerDocument() != null) {
				doc.quirksMode(context.ownerDocument().quirksMode());
			}

			// initialise the tokeniser state:
			final String contextTag = context.tagName();
			if (StringUtil.in(contextTag, "title", "textarea")) {
				tokeniser.transition(TokeniserState.Rcdata);
			} else if (StringUtil.in(contextTag, "iframe", "noembed", "noframes", "style", "xmp")) {
				tokeniser.transition(TokeniserState.Rawtext);
			} else if (contextTag.equals("script")) {
				tokeniser.transition(TokeniserState.ScriptData);
			} else if (contextTag.equals(("noscript"))) {
				tokeniser.transition(TokeniserState.Data); // if scripting enabled,
			} else if (contextTag.equals("plaintext")) {
				tokeniser.transition(TokeniserState.Data);
			} else {
				tokeniser.transition(TokeniserState.Data); // default
			}

			root = new Element(Tag.valueOf("html", settings), baseUri);
			doc.appendChild(root);
			stack.add(root);
			resetInsertionMode();

			// setup form element to nearest form on context (up ancestor chain).
			// ensures form controls are associated
			// with form correctly
			final Elements contextChain = context.parents();
			contextChain.add(0, context);
			for (final Element parent : contextChain) {
				if (parent instanceof FormElement) {
					formElement = (FormElement) parent;
					break;
				}
			}
		}

		runParser();
		if (context != null) {
			return root.childNodes();
		} else {
			return doc.childNodes();
		}
	}

	@Override
	protected boolean process(final Token token) {
		currentToken = token;
		return this.state.process(token, this);
	}

	boolean process(final Token token, final HtmlTreeBuilderState state) {
		currentToken = token;
		return state.process(token, this);
	}

	void transition(final HtmlTreeBuilderState state) {
		this.state = state;
	}

	HtmlTreeBuilderState state() {
		return state;
	}

	void markInsertionMode() {
		originalState = state;
	}

	HtmlTreeBuilderState originalState() {
		return originalState;
	}

	void framesetOk(final boolean framesetOk) {
		this.framesetOk = framesetOk;
	}

	boolean framesetOk() {
		return framesetOk;
	}

	Document getDocument() {
		return doc;
	}

	String getBaseUri() {
		return baseUri;
	}

	void maybeSetBaseUri(final Element base) {
		if (baseUriSetFromDoc) {
			return;
		}

		final String href = base.absUrl("href");
		if (href.length() != 0) { // ignore <base target> etc
			baseUri = href;
			baseUriSetFromDoc = true;
			doc.setBaseUri(href); // set on the doc so doc.createElement(Tag) will
											// get updated base, and to update all
											// descendants
		}
	}

	boolean isFragmentParsing() {
		return fragmentParsing;
	}

	void error(final HtmlTreeBuilderState state) {
		if (errors.canAddError()) {
			errors.add(new ParseError(reader.pos(), "Unexpected token [%s] when in state [%s]",
					currentToken.tokenType(), state));
		}
	}

	Element insert(final Token.StartTag startTag) {
		// handle empty unknown tags
		// when the spec expects an empty tag, will directly hit insertEmpty, so
		// won't generate this fake end tag.
		if (startTag.isSelfClosing()) {
			final Element el = insertEmpty(startTag);
			stack.add(el);
			tokeniser.transition(TokeniserState.Data); // handles <script />,
																		// otherwise needs
																		// breakout steps from
																		// script data
			tokeniser.emit(emptyEnd.reset().name(el.tagName())); // ensure we get
																					// out of
																					// whatever state
																					// we are in.
																					// emitted for
																					// yielded
																					// processing
			return el;
		}

		final Element el = new Element(Tag.valueOf(startTag.name(), settings), baseUri,
				settings.normalizeAttributes(startTag.attributes));
		insert(el);
		return el;
	}

	Element insertStartTag(final String startTagName) {
		final Element el = new Element(Tag.valueOf(startTagName, settings), baseUri);
		insert(el);
		return el;
	}

	void insert(final Element el) {
		insertNode(el);
		stack.add(el);
	}

	Element insertEmpty(final Token.StartTag startTag) {
		final Tag tag = Tag.valueOf(startTag.name(), settings);
		final Element el = new Element(tag, baseUri, startTag.attributes);
		insertNode(el);
		if (startTag.isSelfClosing()) {
			if (tag.isKnownTag()) {
				if (!tag.isEmpty()) {
					tokeniser.error("Tag cannot be self closing; not a void tag");
				}
			} else {
				tag.setSelfClosing();
			}
		}
		return el;
	}

	FormElement insertForm(final Token.StartTag startTag, final boolean onStack) {
		final Tag tag = Tag.valueOf(startTag.name(), settings);
		final FormElement el = new FormElement(tag, baseUri, startTag.attributes);
		setFormElement(el);
		insertNode(el);
		if (onStack) {
			stack.add(el);
		}
		return el;
	}

	void insert(final Token.Comment commentToken) {
		final Comment comment = new Comment(commentToken.getData());
		insertNode(comment);
	}

	void insert(final Token.Character characterToken) {
		Node node;
		// characters in script and style go in as datanodes, not text nodes
		final String tagName = currentElement().tagName();
		final String data = characterToken.getData();

		if (characterToken.isCData()) {
			node = new CDataNode(data);
		} else if (tagName.equals("script") || tagName.equals("style")) {
			node = new DataNode(data);
		} else {
			node = new TextNode(data);
		}
		currentElement().appendChild(node); // doesn't use insertNode, because we
														// don't foster these; and will always
														// have a stack.
	}

	private void insertNode(final Node node) {
		// if the stack hasn't been set up yet, elements (doctype, comments) go
		// into the doc
		if (stack.size() == 0) {
			doc.appendChild(node);
		} else if (isFosterInserts()) {
			insertInFosterParent(node);
		} else {
			currentElement().appendChild(node);
		}

		// connect form controls to their form element
		if (node instanceof Element && ((Element) node).tag().isFormListed()) {
			if (formElement != null) {
				formElement.addElement((Element) node);
			}
		}
	}

	Element pop() {
		final int size = stack.size();
		return stack.remove(size - 1);
	}

	void push(final Element element) {
		stack.add(element);
	}

	ArrayList<Element> getStack() {
		return stack;
	}

	boolean onStack(final Element el) {
		return isElementInQueue(stack, el);
	}

	private boolean isElementInQueue(final ArrayList<Element> queue, final Element element) {
		for (int pos = queue.size() - 1; pos >= 0; pos--) {
			final Element next = queue.get(pos);
			if (next == element) {
				return true;
			}
		}
		return false;
	}

	Element getFromStack(final String elName) {
		for (int pos = stack.size() - 1; pos >= 0; pos--) {
			final Element next = stack.get(pos);
			if (next.nodeName().equals(elName)) {
				return next;
			}
		}
		return null;
	}

	boolean removeFromStack(final Element el) {
		for (int pos = stack.size() - 1; pos >= 0; pos--) {
			final Element next = stack.get(pos);
			if (next == el) {
				stack.remove(pos);
				return true;
			}
		}
		return false;
	}

	void popStackToClose(final String elName) {
		for (int pos = stack.size() - 1; pos >= 0; pos--) {
			final Element next = stack.get(pos);
			stack.remove(pos);
			if (next.nodeName().equals(elName)) {
				break;
			}
		}
	}

	// elnames is sorted, comes from Constants
	void popStackToClose(final String... elNames) {
		for (int pos = stack.size() - 1; pos >= 0; pos--) {
			final Element next = stack.get(pos);
			stack.remove(pos);
			if (inSorted(next.nodeName(), elNames)) {
				break;
			}
		}
	}

	void popStackToBefore(final String elName) {
		for (int pos = stack.size() - 1; pos >= 0; pos--) {
			final Element next = stack.get(pos);
			if (next.nodeName().equals(elName)) {
				break;
			} else {
				stack.remove(pos);
			}
		}
	}

	void clearStackToTableContext() {
		clearStackToContext("table");
	}

	void clearStackToTableBodyContext() {
		clearStackToContext("tbody", "tfoot", "thead", "template");
	}

	void clearStackToTableRowContext() {
		clearStackToContext("tr", "template");
	}

	private void clearStackToContext(final String... nodeNames) {
		for (int pos = stack.size() - 1; pos >= 0; pos--) {
			final Element next = stack.get(pos);
			if (StringUtil.in(next.nodeName(), nodeNames) || next.nodeName().equals("html")) {
				break;
			} else {
				stack.remove(pos);
			}
		}
	}

	Element aboveOnStack(final Element el) {
		assert onStack(el);
		for (int pos = stack.size() - 1; pos >= 0; pos--) {
			final Element next = stack.get(pos);
			if (next == el) {
				return stack.get(pos - 1);
			}
		}
		return null;
	}

	void insertOnStackAfter(final Element after, final Element in) {
		final int i = stack.lastIndexOf(after);
		Validate.isTrue(i != -1);
		stack.add(i + 1, in);
	}

	void replaceOnStack(final Element out, final Element in) {
		replaceInQueue(stack, out, in);
	}

	private void replaceInQueue(final ArrayList<Element> queue, final Element out,
			final Element in) {
		final int i = queue.lastIndexOf(out);
		Validate.isTrue(i != -1);
		queue.set(i, in);
	}

	void resetInsertionMode() {
		boolean last = false;
		for (int pos = stack.size() - 1; pos >= 0; pos--) {
			Element node = stack.get(pos);
			if (pos == 0) {
				last = true;
				node = contextElement;
			}
			final String name = node.nodeName();
			if ("select".equals(name)) {
				transition(HtmlTreeBuilderState.InSelect);
				break; // frag
			} else if (("td".equals(name) || "th".equals(name) && !last)) {
				transition(HtmlTreeBuilderState.InCell);
				break;
			} else if ("tr".equals(name)) {
				transition(HtmlTreeBuilderState.InRow);
				break;
			} else if ("tbody".equals(name) || "thead".equals(name) || "tfoot".equals(name)) {
				transition(HtmlTreeBuilderState.InTableBody);
				break;
			} else if ("caption".equals(name)) {
				transition(HtmlTreeBuilderState.InCaption);
				break;
			} else if ("colgroup".equals(name)) {
				transition(HtmlTreeBuilderState.InColumnGroup);
				break; // frag
			} else if ("table".equals(name)) {
				transition(HtmlTreeBuilderState.InTable);
				break;
			} else if ("head".equals(name)) {
				transition(HtmlTreeBuilderState.InBody);
				break; // frag
			} else if ("body".equals(name)) {
				transition(HtmlTreeBuilderState.InBody);
				break;
			} else if ("frameset".equals(name)) {
				transition(HtmlTreeBuilderState.InFrameset);
				break; // frag
			} else if ("html".equals(name)) {
				transition(HtmlTreeBuilderState.BeforeHead);
				break; // frag
			} else if (last) {
				transition(HtmlTreeBuilderState.InBody);
				break; // frag
			}
		}
	}

	// todo: tidy up in specific scope methods
	private final String[] specificScopeTarget = { null };

	private boolean inSpecificScope(final String targetName, final String[] baseTypes,
			final String[] extraTypes) {
		specificScopeTarget[0] = targetName;
		return inSpecificScope(specificScopeTarget, baseTypes, extraTypes);
	}

	private boolean inSpecificScope(final String[] targetNames, final String[] baseTypes,
			final String[] extraTypes) {
		// https://html.spec.whatwg.org/multipage/parsing.html#has-an-element-in-the-specific-scope
		final int bottom = stack.size() - 1;
		final int top = bottom > MaxScopeSearchDepth ? bottom - MaxScopeSearchDepth : 0;
		// don't walk too far up the tree

		for (int pos = bottom; pos >= top; pos--) {
			final String elName = stack.get(pos).nodeName();
			if (inSorted(elName, targetNames)) {
				return true;
			}
			if (inSorted(elName, baseTypes)) {
				return false;
			}
			if (extraTypes != null && inSorted(elName, extraTypes)) {
				return false;
			}
		}
		// Validate.fail("Should not be reachable"); // would end up false because
		// hitting 'html' at root (basetypes)
		return false;
	}

	boolean inScope(final String[] targetNames) {
		return inSpecificScope(targetNames, TagsSearchInScope, null);
	}

	boolean inScope(final String targetName) {
		return inScope(targetName, null);
	}

	boolean inScope(final String targetName, final String[] extras) {
		return inSpecificScope(targetName, TagsSearchInScope, extras);
		// todo: in mathml namespace: mi, mo, mn, ms, mtext annotation-xml
		// todo: in svg namespace: forignOjbect, desc, title
	}

	boolean inListItemScope(final String targetName) {
		return inScope(targetName, TagSearchList);
	}

	boolean inButtonScope(final String targetName) {
		return inScope(targetName, TagSearchButton);
	}

	boolean inTableScope(final String targetName) {
		return inSpecificScope(targetName, TagSearchTableScope, null);
	}

	boolean inSelectScope(final String targetName) {
		for (int pos = stack.size() - 1; pos >= 0; pos--) {
			final Element el = stack.get(pos);
			final String elName = el.nodeName();
			if (elName.equals(targetName)) {
				return true;
			}
			if (!inSorted(elName, TagSearchSelectScope)) {
				return false;
			}
		}
		Validate.fail("Should not be reachable");
		return false;
	}

	void setHeadElement(final Element headElement) {
		this.headElement = headElement;
	}

	Element getHeadElement() {
		return headElement;
	}

	boolean isFosterInserts() {
		return fosterInserts;
	}

	void setFosterInserts(final boolean fosterInserts) {
		this.fosterInserts = fosterInserts;
	}

	FormElement getFormElement() {
		return formElement;
	}

	void setFormElement(final FormElement formElement) {
		this.formElement = formElement;
	}

	void newPendingTableCharacters() {
		pendingTableCharacters = new ArrayList<>();
	}

	List<String> getPendingTableCharacters() {
		return pendingTableCharacters;
	}

	void setPendingTableCharacters(final List<String> pendingTableCharacters) {
		this.pendingTableCharacters = pendingTableCharacters;
	}

	/**
	 * 11.2.5.2 Closing elements that have implied end tags
	 * <p/>
	 * When the steps below require the UA to generate implied end tags, then,
	 * while the current node is a dd element, a
	 * dt element, an li element, an option element, an optgroup element, a p
	 * element, an rp element, or an rt element,
	 * the UA must pop the current node off the stack of open elements.
	 * 
	 * @param excludeTag
	 *        If a step requires the UA to generate implied end tags but lists an
	 *        element to exclude from the
	 *        process, then the UA must perform the above steps as if that
	 *        element was not in the above list.
	 */
	void generateImpliedEndTags(final String excludeTag) {
		while ((excludeTag != null && !currentElement().nodeName().equals(excludeTag))
				&& inSorted(currentElement().nodeName(), TagSearchEndTags)) {
			pop();
		}
	}

	void generateImpliedEndTags() {
		generateImpliedEndTags(null);
	}

	boolean isSpecial(final Element el) {
		// todo: mathml's mi, mo, mn
		// todo: svg's foreigObject, desc, title
		final String name = el.nodeName();
		return inSorted(name, TagSearchSpecial);
	}

	Element lastFormattingElement() {
		return formattingElements.size() > 0 ? formattingElements.get(formattingElements.size() - 1)
				: null;
	}

	Element removeLastFormattingElement() {
		final int size = formattingElements.size();
		if (size > 0) {
			return formattingElements.remove(size - 1);
		} else {
			return null;
		}
	}

	// active formatting elements
	void pushActiveFormattingElements(final Element in) {
		int numSeen = 0;
		for (int pos = formattingElements.size() - 1; pos >= 0; pos--) {
			final Element el = formattingElements.get(pos);
			if (el == null) {
				break;
			}

			if (isSameFormattingElement(in, el)) {
				numSeen++;
			}

			if (numSeen == 3) {
				formattingElements.remove(pos);
				break;
			}
		}
		formattingElements.add(in);
	}

	private boolean isSameFormattingElement(final Element a, final Element b) {
		// same if: same namespace, tag, and attributes. Element.equals only
		// checks tag, might in future check children
		return a.nodeName().equals(b.nodeName()) &&
		// a.namespace().equals(b.namespace()) &&
				a.attributes().equals(b.attributes());
		// todo: namespaces
	}

	void reconstructFormattingElements() {
		final Element last = lastFormattingElement();
		if (last == null || onStack(last)) {
			return;
		}

		Element entry = last;
		final int size = formattingElements.size();
		int pos = size - 1;
		boolean skip = false;
		while (true) {
			if (pos == 0) { // step 4. if none before, skip to 8
				skip = true;
				break;
			}
			entry = formattingElements.get(--pos); // step 5. one earlier than
																// entry
			if (entry == null || onStack(entry)) {
				// stack
				break; // jump to 8, else continue back to 4
			}
		}
		while (true) {
			if (!skip) {
				entry = formattingElements.get(++pos);
			}
			Validate.notNull(entry); // should not occur, as we break at last
												// element

			// 8. create new element from element, 9 insert into current node, onto
			// stack
			skip = false; // can only skip increment from 4.
			final Element newEl = insertStartTag(entry.nodeName()); // todo: avoid
			// fostering here?
			// newEl.namespace(entry.namespace()); // todo: namespaces
			newEl.attributes().addAll(entry.attributes());

			// 10. replace entry with new entry
			formattingElements.set(pos, newEl);

			// 11
			if (pos == size - 1) {
				break;
			}
		}
	}

	void clearFormattingElementsToLastMarker() {
		while (!formattingElements.isEmpty()) {
			final Element el = removeLastFormattingElement();
			if (el == null) {
				break;
			}
		}
	}

	void removeFromActiveFormattingElements(final Element el) {
		for (int pos = formattingElements.size() - 1; pos >= 0; pos--) {
			final Element next = formattingElements.get(pos);
			if (next == el) {
				formattingElements.remove(pos);
				break;
			}
		}
	}

	boolean isInActiveFormattingElements(final Element el) {
		return isElementInQueue(formattingElements, el);
	}

	Element getActiveFormattingElement(final String nodeName) {
		for (int pos = formattingElements.size() - 1; pos >= 0; pos--) {
			final Element next = formattingElements.get(pos);
			if (next == null) {
				break;
			} else if (next.nodeName().equals(nodeName)) {
				return next;
			}
		}
		return null;
	}

	void replaceActiveFormattingElement(final Element out, final Element in) {
		replaceInQueue(formattingElements, out, in);
	}

	void insertMarkerToFormattingElements() {
		formattingElements.add(null);
	}

	void insertInFosterParent(final Node in) {
		Element fosterParent;
		final Element lastTable = getFromStack("table");
		boolean isLastTableParent = false;
		if (lastTable != null) {
			if (lastTable.parent() != null) {
				fosterParent = lastTable.parent();
				isLastTableParent = true;
			} else {
				fosterParent = aboveOnStack(lastTable);
			}
		} else { // no table == frag
			fosterParent = stack.get(0);
		}

		if (isLastTableParent) {
			Validate.notNull(lastTable); // last table cannot be null by this
													// point.
			lastTable.before(in);
		} else {
			fosterParent.appendChild(in);
		}
	}

	@Override
	public String toString() {
		return "TreeBuilder{" + "currentToken=" + currentToken + ", state=" + state
				+ ", currentElement=" + currentElement() + '}';
	}
}

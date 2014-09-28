package net.simpleframework.lib.org.jsoup.safety;

import net.simpleframework.lib.org.jsoup.helper.Validate;
import net.simpleframework.lib.org.jsoup.nodes.Attribute;
import net.simpleframework.lib.org.jsoup.nodes.Attributes;
import net.simpleframework.lib.org.jsoup.nodes.DataNode;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.nodes.Node;
import net.simpleframework.lib.org.jsoup.nodes.TextNode;
import net.simpleframework.lib.org.jsoup.parser.Tag;
import net.simpleframework.lib.org.jsoup.select.NodeTraversor;
import net.simpleframework.lib.org.jsoup.select.NodeVisitor;

/**
 * The whitelist based HTML cleaner. Use to ensure that end-user provided HTML
 * contains only the elements and attributes
 * that you are expecting; no junk, and no cross-site scripting attacks!
 * <p/>
 * The HTML cleaner parses the input as HTML and then runs it through a
 * white-list, so the output HTML can only contain HTML that is allowed by the
 * whitelist.
 * <p/>
 * It is assumed that the input HTML is a body fragment; the clean methods only
 * pull from the source's body, and the canned white-lists only allow body
 * contained tags.
 * <p/>
 * Rather than interacting directly with a Cleaner object, generally see the
 * {@code clean} methods in {@link org.jsoup.Jsoup}.
 */
public class Cleaner {
	private final Whitelist whitelist;

	/**
	 * Create a new cleaner, that sanitizes documents using the supplied
	 * whitelist.
	 * 
	 * @param whitelist
	 *        white-list to clean with
	 */
	public Cleaner(final Whitelist whitelist) {
		Validate.notNull(whitelist);
		this.whitelist = whitelist;
	}

	/**
	 * Creates a new, clean document, from the original dirty document,
	 * containing only elements allowed by the whitelist.
	 * The original document is not modified. Only elements from the dirt
	 * document's <code>body</code> are used.
	 * 
	 * @param dirtyDocument
	 *        Untrusted base document to clean.
	 * @return cleaned document.
	 */
	public Document clean(final Document dirtyDocument) {
		Validate.notNull(dirtyDocument);

		final Document clean = Document.createShell(dirtyDocument.baseUri());
		if (dirtyDocument.body() != null) {
			// the clean doc will have empty body.
			copySafeNodes(dirtyDocument.body(), clean.body());
		}

		return clean;
	}

	/**
	 * Determines if the input document is valid, against the whitelist. It is
	 * considered valid if all the tags and attributes
	 * in the input HTML are allowed by the whitelist.
	 * <p/>
	 * This method can be used as a validator for user input forms. An invalid
	 * document will still be cleaned successfully using the
	 * {@link #clean(Document)} document. If using as a validator, it is
	 * recommended to still clean the document to ensure enforced attributes are
	 * set correctly, and that the output is tidied.
	 * 
	 * @param dirtyDocument
	 *        document to test
	 * @return true if no tags or attributes need to be removed; false if they do
	 */
	public boolean isValid(final Document dirtyDocument) {
		Validate.notNull(dirtyDocument);

		final Document clean = Document.createShell(dirtyDocument.baseUri());
		final int numDiscarded = copySafeNodes(dirtyDocument.body(), clean.body());
		return numDiscarded == 0;
	}

	/**
	 * Iterates the input and copies trusted nodes (tags, attributes, text) into
	 * the destination.
	 */
	private final class CleaningVisitor implements NodeVisitor {
		private int numDiscarded = 0;
		private final Element root;
		private Element destination; // current element to append nodes to

		private CleaningVisitor(final Element root, final Element destination) {
			this.root = root;
			this.destination = destination;
		}

		@Override
		public void head(final Node source, final int depth) {
			if (source instanceof Element) {
				final Element sourceEl = (Element) source;

				if (whitelist.isSafeTag(sourceEl.tagName())) { // safe, clone and
																				// copy safe attrs
					final ElementMeta meta = createSafeElement(sourceEl);
					final Element destChild = meta.el;
					destination.appendChild(destChild);

					numDiscarded += meta.numAttribsDiscarded;
					destination = destChild;
				} else if (source != root) { // not a safe tag, so don't add. don't
														// count root against discarded.
					numDiscarded++;
				}
			} else if (source instanceof TextNode) {
				final TextNode sourceText = (TextNode) source;
				final TextNode destText = new TextNode(sourceText.getWholeText(), source.baseUri());
				destination.appendChild(destText);
			} else if (source instanceof DataNode && whitelist.isSafeTag(source.parent().nodeName())) {
				final DataNode sourceData = (DataNode) source;
				final DataNode destData = new DataNode(sourceData.getWholeData(), source.baseUri());
				destination.appendChild(destData);
			} else { // else, we don't care about comments, xml proc instructions,
						// etc
				numDiscarded++;
			}
		}

		@Override
		public void tail(final Node source, final int depth) {
			if (source instanceof Element && whitelist.isSafeTag(source.nodeName())) {
				destination = destination.parent(); // would have descended, so pop
																// destination stack
			}
		}
	}

	private int copySafeNodes(final Element source, final Element dest) {
		final CleaningVisitor cleaningVisitor = new CleaningVisitor(source, dest);
		final NodeTraversor traversor = new NodeTraversor(cleaningVisitor);
		traversor.traverse(source);
		return cleaningVisitor.numDiscarded;
	}

	private ElementMeta createSafeElement(final Element sourceEl) {
		final String sourceTag = sourceEl.tagName();
		final Attributes destAttrs = new Attributes();
		final Element dest = new Element(Tag.valueOf(sourceTag), sourceEl.baseUri(), destAttrs);
		int numDiscarded = 0;

		final Attributes sourceAttrs = sourceEl.attributes();
		for (final Attribute sourceAttr : sourceAttrs) {
			if (whitelist.isSafeAttribute(sourceTag, sourceEl, sourceAttr)) {
				destAttrs.put(sourceAttr);
			} else {
				numDiscarded++;
			}
		}
		final Attributes enforcedAttrs = whitelist.getEnforcedAttributes(sourceTag);
		destAttrs.addAll(enforcedAttrs);

		return new ElementMeta(dest, numDiscarded);
	}

	private static class ElementMeta {
		Element el;
		int numAttribsDiscarded;

		ElementMeta(final Element el, final int numAttribsDiscarded) {
			this.el = el;
			this.numAttribsDiscarded = numAttribsDiscarded;
		}
	}

}

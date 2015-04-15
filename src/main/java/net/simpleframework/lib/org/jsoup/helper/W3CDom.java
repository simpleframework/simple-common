package net.simpleframework.lib.org.jsoup.helper;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.simpleframework.lib.org.jsoup.nodes.Attribute;
import net.simpleframework.lib.org.jsoup.select.NodeTraversor;
import net.simpleframework.lib.org.jsoup.select.NodeVisitor;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Helper class to transform a
 * {@link net.simpleframework.lib.org.jsoup.nodes.Document} to a
 * {@link org.w3c.dom.Document org.w3c.dom.Document},
 * for integration with toolsets that use the W3C DOM.
 * <p>
 * This class is currently <b>experimental</b>, please provide feedback on
 * utility and any problems experienced.
 * </p>
 */
public class W3CDom {
	protected DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	/**
	 * Convert a jsoup Document to a W3C Document.
	 * 
	 * @param in
	 *        jsoup doc
	 * @return w3c doc
	 */
	public Document fromJsoup(final net.simpleframework.lib.org.jsoup.nodes.Document in) {
		Validate.notNull(in);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			final Document out = builder.newDocument();
			convert(in, out);
			return out;
		} catch (final ParserConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Converts a jsoup document into the provided W3C Document. If required, you
	 * can set options on the output document
	 * before converting.
	 * 
	 * @param in
	 *        jsoup doc
	 * @param out
	 *        w3c doc
	 * @see net.simpleframework.lib.org.jsoup.helper.W3CDom#fromJsoup(net.simpleframework.lib.org.jsoup.nodes.Document)
	 */
	public void convert(final net.simpleframework.lib.org.jsoup.nodes.Document in, final Document out) {
		if (!StringUtil.isBlank(in.location())) {
			out.setDocumentURI(in.location());
		}

		final net.simpleframework.lib.org.jsoup.nodes.Element rootEl = in.child(0); // skip
																												// the
																												// #root
																												// node
		final NodeTraversor traversor = new NodeTraversor(new W3CBuilder(out));
		traversor.traverse(rootEl);
	}

	/**
	 * Implements the conversion by walking the input.
	 */
	protected class W3CBuilder implements NodeVisitor {
		private final Document doc;
		private Element dest;

		public W3CBuilder(final Document doc) {
			this.doc = doc;
		}

		@Override
		public void head(final net.simpleframework.lib.org.jsoup.nodes.Node source, final int depth) {
			if (source instanceof net.simpleframework.lib.org.jsoup.nodes.Element) {
				final net.simpleframework.lib.org.jsoup.nodes.Element sourceEl = (net.simpleframework.lib.org.jsoup.nodes.Element) source;
				final Element el = doc.createElement(sourceEl.tagName());
				copyAttributes(sourceEl, el);
				if (dest == null) { // sets up the root
					doc.appendChild(el);
				} else {
					dest.appendChild(el);
				}
				dest = el; // descend
			} else if (source instanceof net.simpleframework.lib.org.jsoup.nodes.TextNode) {
				final net.simpleframework.lib.org.jsoup.nodes.TextNode sourceText = (net.simpleframework.lib.org.jsoup.nodes.TextNode) source;
				final Text text = doc.createTextNode(sourceText.getWholeText());
				dest.appendChild(text);
			} else if (source instanceof net.simpleframework.lib.org.jsoup.nodes.Comment) {
				final net.simpleframework.lib.org.jsoup.nodes.Comment sourceComment = (net.simpleframework.lib.org.jsoup.nodes.Comment) source;
				final Comment comment = doc.createComment(sourceComment.getData());
				dest.appendChild(comment);
			} else if (source instanceof net.simpleframework.lib.org.jsoup.nodes.DataNode) {
				final net.simpleframework.lib.org.jsoup.nodes.DataNode sourceData = (net.simpleframework.lib.org.jsoup.nodes.DataNode) source;
				final Text node = doc.createTextNode(sourceData.getWholeData());
				dest.appendChild(node);
			} else {
				// unhandled
			}
		}

		@Override
		public void tail(final net.simpleframework.lib.org.jsoup.nodes.Node source, final int depth) {
			if (source instanceof net.simpleframework.lib.org.jsoup.nodes.Element
					&& dest.getParentNode() instanceof Element) {
				dest = (Element) dest.getParentNode(); // undescend. cromulent.
			}
		}

		private void copyAttributes(final net.simpleframework.lib.org.jsoup.nodes.Node source,
				final Element el) {
			for (final Attribute attribute : source.attributes()) {
				el.setAttribute(attribute.getKey(), attribute.getValue());
			}
		}
	}

	/**
	 * Serialize a W3C document to a String.
	 * 
	 * @param doc
	 *        Document
	 * @return Document as string
	 */
	public String asString(final Document doc) {
		try {
			final DOMSource domSource = new DOMSource(doc);
			final StringWriter writer = new StringWriter();
			final StreamResult result = new StreamResult(writer);
			final TransformerFactory tf = TransformerFactory.newInstance();
			final Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (final TransformerException e) {
			throw new IllegalStateException(e);
		}
	}
}

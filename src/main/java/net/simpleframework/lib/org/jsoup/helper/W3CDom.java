package net.simpleframework.lib.org.jsoup.helper;

import static javax.xml.transform.OutputKeys.METHOD;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import net.simpleframework.lib.org.jsoup.internal.StringUtil;
import net.simpleframework.lib.org.jsoup.nodes.Attribute;
import net.simpleframework.lib.org.jsoup.nodes.Attributes;
import net.simpleframework.lib.org.jsoup.select.NodeTraversor;
import net.simpleframework.lib.org.jsoup.select.NodeVisitor;

/**
 * Helper class to transform a
 * {@link net.simpleframework.lib.org.jsoup.nodes.Document} to a
 * {@link org.w3c.dom.Document org.w3c.dom.Document},
 * for integration with toolsets that use the W3C DOM.
 */
public class W3CDom {
	protected DocumentBuilderFactory factory;

	public W3CDom() {
		factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
	}

	/**
	 * Converts a jsoup DOM to a W3C DOM
	 *
	 * @param in
	 *        jsoup Document
	 * @return W3C Document
	 */
	public static Document convert(final net.simpleframework.lib.org.jsoup.nodes.Document in) {
		return (new W3CDom().fromJsoup(in));
	}

	/**
	 * Serialize a W3C document to a String. Provide Properties to define output
	 * settings including if HTML or XML. If
	 * you don't provide the properties ({@code null}), the output will be
	 * auto-detected based on the content of the
	 * document.
	 *
	 * @param doc
	 *        Document
	 * @param properties
	 *        (optional/nullable) the output properties to use. See {@link
	 *        Transformer#setOutputProperties(Properties)} and {@link OutputKeys}
	 * @return Document as string
	 * @see #OutputHtml
	 * @see #OutputXml
	 * @see OutputKeys#ENCODING
	 * @see OutputKeys#OMIT_XML_DECLARATION
	 * @see OutputKeys#STANDALONE
	 * @see OutputKeys#STANDALONE
	 * @see OutputKeys#DOCTYPE_PUBLIC
	 * @see OutputKeys#DOCTYPE_PUBLIC
	 * @see OutputKeys#CDATA_SECTION_ELEMENTS
	 * @see OutputKeys#INDENT
	 * @see OutputKeys#MEDIA_TYPE
	 */
	public static String asString(final Document doc, final Map<String, String> properties) {
		try {
			final DOMSource domSource = new DOMSource(doc);
			final StringWriter writer = new StringWriter();
			final StreamResult result = new StreamResult(writer);
			final TransformerFactory tf = TransformerFactory.newInstance();
			final Transformer transformer = tf.newTransformer();
			if (properties != null) {
				transformer.setOutputProperties(propertiesFromMap(properties));
			}

			if (doc.getDoctype() != null) {
				final DocumentType doctype = doc.getDoctype();
				if (!StringUtil.isBlank(doctype.getPublicId())) {
					transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
				}
				if (!StringUtil.isBlank(doctype.getSystemId())) {
					transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
				} else if (doctype.getName().equalsIgnoreCase("html")
						&& StringUtil.isBlank(doctype.getPublicId())
						&& StringUtil.isBlank(doctype.getSystemId())) {
					transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "about:legacy-compat");
				}
			}

			transformer.transform(domSource, result);
			return writer.toString();

		} catch (final TransformerException e) {
			throw new IllegalStateException(e);
		}
	}

	static Properties propertiesFromMap(final Map<String, String> map) {
		final Properties props = new Properties();
		props.putAll(map);
		return props;
	}

	/** Canned default for HTML output. */
	public static HashMap<String, String> OutputHtml() {
		return methodMap("html");
	}

	/** Canned default for XML output. */
	public static HashMap<String, String> OutputXml() {
		return methodMap("xml");
	}

	private static HashMap<String, String> methodMap(final String method) {
		final HashMap<String, String> map = new HashMap<>();
		map.put(METHOD, method);
		return map;
	}

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
			final DOMImplementation impl = builder.getDOMImplementation();
			Document out;

			out = builder.newDocument();
			final net.simpleframework.lib.org.jsoup.nodes.DocumentType doctype = in.documentType();
			if (doctype != null) {
				final org.w3c.dom.DocumentType documentType = impl.createDocumentType(doctype.name(),
						doctype.publicId(), doctype.systemId());
				out.appendChild(documentType);
			}
			out.setXmlStandalone(true);

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
	public void convert(final net.simpleframework.lib.org.jsoup.nodes.Document in,
			final Document out) {
		if (!StringUtil.isBlank(in.location())) {
			out.setDocumentURI(in.location());
		}

		final net.simpleframework.lib.org.jsoup.nodes.Element rootEl = in.child(0); // skip
		// the
		// #root
		// node
		NodeTraversor.traverse(new W3CBuilder(out), rootEl);
	}

	/**
	 * Serialize a W3C document to a String. The output format will be XML or
	 * HTML depending on the content of the doc.
	 *
	 * @param doc
	 *        Document
	 * @return Document as string
	 * @see W3CDom#asString(Document, Map)
	 */
	public String asString(final Document doc) {
		return asString(doc, null);
	}

	/**
	 * Implements the conversion by walking the input.
	 */
	protected static class W3CBuilder implements NodeVisitor {
		private static final String xmlnsKey = "xmlns";
		private static final String xmlnsPrefix = "xmlns:";

		private final Document doc;
		private final Stack<HashMap<String, String>> namespacesStack = new Stack<>(); // stack
																												// of
																												// namespaces,
																												// prefix
																												// =>
																												// urn
		private Element dest;

		public W3CBuilder(final Document doc) {
			this.doc = doc;
			this.namespacesStack.push(new HashMap<String, String>());
		}

		@Override
		public void head(final net.simpleframework.lib.org.jsoup.nodes.Node source, final int depth) {
			namespacesStack.push(new HashMap<>(namespacesStack.peek())); // inherit
																								// from
																								// above
																								// on
																								// the
																								// stack
			if (source instanceof net.simpleframework.lib.org.jsoup.nodes.Element) {
				final net.simpleframework.lib.org.jsoup.nodes.Element sourceEl = (net.simpleframework.lib.org.jsoup.nodes.Element) source;

				final String prefix = updateNamespaces(sourceEl);
				final String namespace = namespacesStack.peek().get(prefix);
				final String tagName = sourceEl.tagName();

				final Element el = namespace == null && tagName.contains(":")
						? doc.createElementNS("", tagName)
						: // doesn't have a real namespace defined
						doc.createElementNS(namespace, tagName);
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
				// not that doctype is not handled here - rather it is used in the
				// initial doc creation
			}
		}

		@Override
		public void tail(final net.simpleframework.lib.org.jsoup.nodes.Node source, final int depth) {
			if (source instanceof net.simpleframework.lib.org.jsoup.nodes.Element
					&& dest.getParentNode() instanceof Element) {
				dest = (Element) dest.getParentNode(); // undescend. cromulent.
			}
			namespacesStack.pop();
		}

		private void copyAttributes(final net.simpleframework.lib.org.jsoup.nodes.Node source,
				final Element el) {
			for (final Attribute attribute : source.attributes()) {
				// valid xml attribute names are: ^[a-zA-Z_:][-a-zA-Z0-9_:.]
				final String key = attribute.getKey().replaceAll("[^-a-zA-Z0-9_:.]", "");
				if (key.matches("[a-zA-Z_:][-a-zA-Z0-9_:.]*")) {
					el.setAttribute(key, attribute.getValue());
				}
			}
		}

		/**
		 * Finds any namespaces defined in this element. Returns any tag prefix.
		 */
		private String updateNamespaces(final net.simpleframework.lib.org.jsoup.nodes.Element el) {
			// scan the element for namespace declarations
			// like: xmlns="blah" or xmlns:prefix="blah"
			final Attributes attributes = el.attributes();
			for (final Attribute attr : attributes) {
				final String key = attr.getKey();
				String prefix;
				if (key.equals(xmlnsKey)) {
					prefix = "";
				} else if (key.startsWith(xmlnsPrefix)) {
					prefix = key.substring(xmlnsPrefix.length());
				} else {
					continue;
				}
				namespacesStack.peek().put(prefix, attr.getValue());
			}

			// get the element prefix if any
			final int pos = el.tagName().indexOf(":");
			return pos > 0 ? el.tagName().substring(0, pos) : "";
		}

	}
}

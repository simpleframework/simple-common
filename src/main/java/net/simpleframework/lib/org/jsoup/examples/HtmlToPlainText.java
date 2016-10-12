package net.simpleframework.lib.org.jsoup.examples;

import java.io.IOException;

import net.simpleframework.lib.org.jsoup.Jsoup;
import net.simpleframework.lib.org.jsoup.helper.StringUtil;
import net.simpleframework.lib.org.jsoup.helper.Validate;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.nodes.Node;
import net.simpleframework.lib.org.jsoup.nodes.TextNode;
import net.simpleframework.lib.org.jsoup.select.Elements;
import net.simpleframework.lib.org.jsoup.select.NodeTraversor;
import net.simpleframework.lib.org.jsoup.select.NodeVisitor;

/**
 * HTML to plain-text. This example program demonstrates the use of jsoup to
 * convert HTML input to lightly-formatted
 * plain-text. That is divergent from the general goal of jsoup's .text()
 * methods, which is to get clean data from a
 * scrape.
 * <p>
 * Note that this is a fairly simplistic formatter -- for real world use you'll
 * want to embrace and extend.
 * </p>
 * <p>
 * To invoke from the command line, assuming you've downloaded the jsoup jar to
 * your current directory:
 * </p>
 * <p>
 * <code>java -cp jsoup.jar net.simpleframework.lib.org.jsoup.examples.HtmlToPlainText url [selector]</code>
 * </p>
 * where <i>url</i> is the URL to fetch, and <i>selector</i> is an optional CSS
 * selector.
 * 
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class HtmlToPlainText {
	private static final String userAgent = "Mozilla/5.0 (jsoup)";
	private static final int timeout = 5 * 1000;

	public static void main(final String... args) throws IOException {
		Validate.isTrue(args.length == 1 || args.length == 2,
				"usage: java -cp jsoup.jar net.simpleframework.lib.org.jsoup.examples.HtmlToPlainText url [selector]");
		final String url = args[0];
		final String selector = args.length == 2 ? args[1] : null;

		// fetch the specified URL and parse to a HTML DOM
		final Document doc = Jsoup.connect(url).userAgent(userAgent).timeout(timeout).get();

		final HtmlToPlainText formatter = new HtmlToPlainText();

		if (selector != null) {
			final Elements elements = doc.select(selector); // get each element
																			// that matches the CSS
																			// selector
			for (final Element element : elements) {
				final String plainText = formatter.getPlainText(element); // format
																								// that
																								// element
																								// to
																								// plain
																								// text
				System.out.println(plainText);
			}
		} else { // format the whole doc
			final String plainText = formatter.getPlainText(doc);
			System.out.println(plainText);
		}
	}

	/**
	 * Format an Element to plain-text
	 * 
	 * @param element
	 *        the root element to format
	 * @return formatted text
	 */
	public String getPlainText(final Element element) {
		final FormattingVisitor formatter = new FormattingVisitor();
		final NodeTraversor traversor = new NodeTraversor(formatter);
		traversor.traverse(element); // walk the DOM, and call .head() and .tail()
												// for each node

		return formatter.toString();
	}

	// the formatting rules, implemented in a breadth-first DOM traverse
	private class FormattingVisitor implements NodeVisitor {
		private static final int maxWidth = 80;
		private int width = 0;
		private final StringBuilder accum = new StringBuilder(); // holds the
																					// accumulated
																					// text

		// hit when the node is first seen
		@Override
		public void head(final Node node, final int depth) {
			final String name = node.nodeName();
			if (node instanceof TextNode) {
				append(((TextNode) node).text()); // TextNodes carry all
																// user-readable text in the
																// DOM.
			} else if (name.equals("li")) {
				append("\n * ");
			} else if (name.equals("dt")) {
				append("  ");
			} else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr")) {
				append("\n");
			}
		}

		// hit when all of the node's children (if any) have been visited
		@Override
		public void tail(final Node node, final int depth) {
			final String name = node.nodeName();
			if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5")) {
				append("\n");
			} else if (name.equals("a")) {
				append(String.format(" <%s>", node.absUrl("href")));
			}
		}

		// appends text to the string builder with a simple word wrap method
		private void append(final String text) {
			if (text.startsWith("\n")) {
				width = 0; // reset counter if starts with a newline. only from
								// formats above, not in natural text
			}
			if (text.equals(" ") && (accum.length() == 0
					|| StringUtil.in(accum.substring(accum.length() - 1), " ", "\n"))) {
				return; // don't accumulate long runs of empty spaces
			}

			if (text.length() + width > maxWidth) { // won't fit, needs to wrap
				final String words[] = text.split("\\s+");
				for (int i = 0; i < words.length; i++) {
					String word = words[i];
					final boolean last = i == words.length - 1;
					if (!last) {
						word = word + " ";
					}
					if (word.length() + width > maxWidth) { // wrap and reset counter
						accum.append("\n").append(word);
						width = word.length();
					} else {
						accum.append(word);
						width += word.length();
					}
				}
			} else { // fits as is, without need to wrap text
				accum.append(text);
				width += text.length();
			}
		}

		@Override
		public String toString() {
			return accum.toString();
		}
	}
}

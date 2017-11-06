package net.simpleframework.lib.org.jsoup.select;

import static net.simpleframework.lib.org.jsoup.select.NodeFilter.FilterResult.CONTINUE;
import static net.simpleframework.lib.org.jsoup.select.NodeFilter.FilterResult.STOP;

import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.nodes.Node;

/**
 * Collects a list of elements that match the supplied criteria.
 *
 * @author Jonathan Hedley
 */
public class Collector {

	private Collector() {
	}

	/**
	 * Build a list of elements, by visiting root and every descendant of root,
	 * and testing it against the evaluator.
	 * 
	 * @param eval
	 *        Evaluator to test elements against
	 * @param root
	 *        root of tree to descend
	 * @return list of matches; empty if none
	 */
	public static Elements collect(final Evaluator eval, final Element root) {
		final Elements elements = new Elements();
		NodeTraversor.traverse(new Accumulator(root, elements, eval), root);
		return elements;
	}

	private static class Accumulator implements NodeVisitor {
		private final Element root;
		private final Elements elements;
		private final Evaluator eval;

		Accumulator(final Element root, final Elements elements, final Evaluator eval) {
			this.root = root;
			this.elements = elements;
			this.eval = eval;
		}

		@Override
		public void head(final Node node, final int depth) {
			if (node instanceof Element) {
				final Element el = (Element) node;
				if (eval.matches(root, el)) {
					elements.add(el);
				}
			}
		}

		@Override
		public void tail(final Node node, final int depth) {
			// void
		}
	}

	public static Element findFirst(final Evaluator eval, final Element root) {
		final FirstFinder finder = new FirstFinder(root, eval);
		NodeTraversor.filter(finder, root);
		return finder.match;
	}

	private static class FirstFinder implements NodeFilter {
		private final Element root;
		private Element match = null;
		private final Evaluator eval;

		FirstFinder(final Element root, final Evaluator eval) {
			this.root = root;
			this.eval = eval;
		}

		@Override
		public FilterResult head(final Node node, final int depth) {
			if (node instanceof Element) {
				final Element el = (Element) node;
				if (eval.matches(root, el)) {
					match = el;
					return STOP;
				}
			}
			return CONTINUE;
		}

		@Override
		public FilterResult tail(final Node node, final int depth) {
			return CONTINUE;
		}
	}

}

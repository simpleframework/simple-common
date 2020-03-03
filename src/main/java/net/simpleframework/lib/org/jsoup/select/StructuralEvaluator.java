package net.simpleframework.lib.org.jsoup.select;

import net.simpleframework.lib.org.jsoup.nodes.Element;

/**
 * Base structural evaluator.
 */
abstract class StructuralEvaluator extends Evaluator {
	Evaluator evaluator;

	static class Root extends Evaluator {
		@Override
		public boolean matches(final Element root, final Element element) {
			return root == element;
		}
	}

	static class Has extends StructuralEvaluator {
		public Has(final Evaluator evaluator) {
			this.evaluator = evaluator;
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			for (final Element e : element.getAllElements()) {
				if (e != element && evaluator.matches(element, e)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return String.format(":has(%s)", evaluator);
		}
	}

	static class Not extends StructuralEvaluator {
		public Not(final Evaluator evaluator) {
			this.evaluator = evaluator;
		}

		@Override
		public boolean matches(final Element root, final Element node) {
			return !evaluator.matches(root, node);
		}

		@Override
		public String toString() {
			return String.format(":not%s", evaluator);
		}
	}

	static class Parent extends StructuralEvaluator {
		public Parent(final Evaluator evaluator) {
			this.evaluator = evaluator;
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			if (root == element) {
				return false;
			}

			Element parent = element.parent();
			while (true) {
				if (evaluator.matches(root, parent)) {
					return true;
				}
				if (parent == root) {
					break;
				}
				parent = parent.parent();
			}
			return false;
		}

		@Override
		public String toString() {
			return String.format(":parent%s", evaluator);
		}
	}

	static class ImmediateParent extends StructuralEvaluator {
		public ImmediateParent(final Evaluator evaluator) {
			this.evaluator = evaluator;
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			if (root == element) {
				return false;
			}

			final Element parent = element.parent();
			return parent != null && evaluator.matches(root, parent);
		}

		@Override
		public String toString() {
			return String.format(":ImmediateParent%s", evaluator);
		}
	}

	static class PreviousSibling extends StructuralEvaluator {
		public PreviousSibling(final Evaluator evaluator) {
			this.evaluator = evaluator;
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			if (root == element) {
				return false;
			}

			Element prev = element.previousElementSibling();

			while (prev != null) {
				if (evaluator.matches(root, prev)) {
					return true;
				}

				prev = prev.previousElementSibling();
			}
			return false;
		}

		@Override
		public String toString() {
			return String.format(":prev*%s", evaluator);
		}
	}

	static class ImmediatePreviousSibling extends StructuralEvaluator {
		public ImmediatePreviousSibling(final Evaluator evaluator) {
			this.evaluator = evaluator;
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			if (root == element) {
				return false;
			}

			final Element prev = element.previousElementSibling();
			return prev != null && evaluator.matches(root, prev);
		}

		@Override
		public String toString() {
			return String.format(":prev%s", evaluator);
		}
	}
}

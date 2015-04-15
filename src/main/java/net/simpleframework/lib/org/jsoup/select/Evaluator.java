package net.simpleframework.lib.org.jsoup.select;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.simpleframework.lib.org.jsoup.helper.Validate;
import net.simpleframework.lib.org.jsoup.nodes.Comment;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.nodes.DocumentType;
import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.nodes.Node;
import net.simpleframework.lib.org.jsoup.nodes.XmlDeclaration;

/**
 * Evaluates that an element matches the selector.
 */
public abstract class Evaluator {
	protected Evaluator() {
	}

	/**
	 * Test if the element meets the evaluator's requirements.
	 *
	 * @param root
	 *        Root of the matching subtree
	 * @param element
	 *        tested element
	 * @return Returns <tt>true</tt> if the requirements are met or
	 *         <tt>false</tt> otherwise
	 */
	public abstract boolean matches(Element root, Element element);

	/**
	 * Evaluator for tag name
	 */
	public static final class Tag extends Evaluator {
		private final String tagName;

		public Tag(final String tagName) {
			this.tagName = tagName;
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return (element.tagName().equals(tagName));
		}

		@Override
		public String toString() {
			return String.format("%s", tagName);
		}
	}

	/**
	 * Evaluator for element id
	 */
	public static final class Id extends Evaluator {
		private final String id;

		public Id(final String id) {
			this.id = id;
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return (id.equals(element.id()));
		}

		@Override
		public String toString() {
			return String.format("#%s", id);
		}

	}

	/**
	 * Evaluator for element class
	 */
	public static final class Class extends Evaluator {
		private final String className;

		public Class(final String className) {
			this.className = className;
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return (element.hasClass(className));
		}

		@Override
		public String toString() {
			return String.format(".%s", className);
		}

	}

	/**
	 * Evaluator for attribute name matching
	 */
	public static final class Attribute extends Evaluator {
		private final String key;

		public Attribute(final String key) {
			this.key = key;
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return element.hasAttr(key);
		}

		@Override
		public String toString() {
			return String.format("[%s]", key);
		}

	}

	/**
	 * Evaluator for attribute name prefix matching
	 */
	public static final class AttributeStarting extends Evaluator {
		private final String keyPrefix;

		public AttributeStarting(final String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			final List<net.simpleframework.lib.org.jsoup.nodes.Attribute> values = element
					.attributes().asList();
			for (final net.simpleframework.lib.org.jsoup.nodes.Attribute attribute : values) {
				if (attribute.getKey().startsWith(keyPrefix)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return String.format("[^%s]", keyPrefix);
		}

	}

	/**
	 * Evaluator for attribute name/value matching
	 */
	public static final class AttributeWithValue extends AttributeKeyPair {
		public AttributeWithValue(final String key, final String value) {
			super(key, value);
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return element.hasAttr(key) && value.equalsIgnoreCase(element.attr(key).trim());
		}

		@Override
		public String toString() {
			return String.format("[%s=%s]", key, value);
		}

	}

	/**
	 * Evaluator for attribute name != value matching
	 */
	public static final class AttributeWithValueNot extends AttributeKeyPair {
		public AttributeWithValueNot(final String key, final String value) {
			super(key, value);
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return !value.equalsIgnoreCase(element.attr(key));
		}

		@Override
		public String toString() {
			return String.format("[%s!=%s]", key, value);
		}

	}

	/**
	 * Evaluator for attribute name/value matching (value prefix)
	 */
	public static final class AttributeWithValueStarting extends AttributeKeyPair {
		public AttributeWithValueStarting(final String key, final String value) {
			super(key, value);
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return element.hasAttr(key) && element.attr(key).toLowerCase().startsWith(value); // value
																															// is
																															// lower
																															// case
																															// already
		}

		@Override
		public String toString() {
			return String.format("[%s^=%s]", key, value);
		}

	}

	/**
	 * Evaluator for attribute name/value matching (value ending)
	 */
	public static final class AttributeWithValueEnding extends AttributeKeyPair {
		public AttributeWithValueEnding(final String key, final String value) {
			super(key, value);
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return element.hasAttr(key) && element.attr(key).toLowerCase().endsWith(value); // value
																														// is
																														// lower
																														// case
		}

		@Override
		public String toString() {
			return String.format("[%s$=%s]", key, value);
		}

	}

	/**
	 * Evaluator for attribute name/value matching (value containing)
	 */
	public static final class AttributeWithValueContaining extends AttributeKeyPair {
		public AttributeWithValueContaining(final String key, final String value) {
			super(key, value);
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return element.hasAttr(key) && element.attr(key).toLowerCase().contains(value); // value
																														// is
																														// lower
																														// case
		}

		@Override
		public String toString() {
			return String.format("[%s*=%s]", key, value);
		}

	}

	/**
	 * Evaluator for attribute name/value matching (value regex matching)
	 */
	public static final class AttributeWithValueMatching extends Evaluator {
		String key;
		Pattern pattern;

		public AttributeWithValueMatching(final String key, final Pattern pattern) {
			this.key = key.trim().toLowerCase();
			this.pattern = pattern;
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return element.hasAttr(key) && pattern.matcher(element.attr(key)).find();
		}

		@Override
		public String toString() {
			return String.format("[%s~=%s]", key, pattern.toString());
		}

	}

	/**
	 * Abstract evaluator for attribute name/value matching
	 */
	public abstract static class AttributeKeyPair extends Evaluator {
		String key;
		String value;

		public AttributeKeyPair(final String key, String value) {
			Validate.notEmpty(key);
			Validate.notEmpty(value);

			this.key = key.trim().toLowerCase();
			if (value.startsWith("\"") && value.endsWith("\"")) {
				value = value.substring(1, value.length() - 1);
			}
			this.value = value.trim().toLowerCase();
		}
	}

	/**
	 * Evaluator for any / all element matching
	 */
	public static final class AllElements extends Evaluator {

		@Override
		public boolean matches(final Element root, final Element element) {
			return true;
		}

		@Override
		public String toString() {
			return "*";
		}
	}

	/**
	 * Evaluator for matching by sibling index number (e {@literal <} idx)
	 */
	public static final class IndexLessThan extends IndexEvaluator {
		public IndexLessThan(final int index) {
			super(index);
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return element.elementSiblingIndex() < index;
		}

		@Override
		public String toString() {
			return String.format(":lt(%d)", index);
		}

	}

	/**
	 * Evaluator for matching by sibling index number (e {@literal >} idx)
	 */
	public static final class IndexGreaterThan extends IndexEvaluator {
		public IndexGreaterThan(final int index) {
			super(index);
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return element.elementSiblingIndex() > index;
		}

		@Override
		public String toString() {
			return String.format(":gt(%d)", index);
		}

	}

	/**
	 * Evaluator for matching by sibling index number (e = idx)
	 */
	public static final class IndexEquals extends IndexEvaluator {
		public IndexEquals(final int index) {
			super(index);
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return element.elementSiblingIndex() == index;
		}

		@Override
		public String toString() {
			return String.format(":eq(%d)", index);
		}

	}

	/**
	 * Evaluator for matching the last sibling (css :last-child)
	 */
	public static final class IsLastChild extends Evaluator {
		@Override
		public boolean matches(final Element root, final Element element) {
			final Element p = element.parent();
			return p != null && !(p instanceof Document)
					&& element.elementSiblingIndex() == p.children().size() - 1;
		}

		@Override
		public String toString() {
			return ":last-child";
		}
	}

	public static final class IsFirstOfType extends IsNthOfType {
		public IsFirstOfType() {
			super(0, 1);
		}

		@Override
		public String toString() {
			return ":first-of-type";
		}
	}

	public static final class IsLastOfType extends IsNthLastOfType {
		public IsLastOfType() {
			super(0, 1);
		}

		@Override
		public String toString() {
			return ":last-of-type";
		}
	}

	public static abstract class CssNthEvaluator extends Evaluator {
		protected final int a, b;

		public CssNthEvaluator(final int a, final int b) {
			this.a = a;
			this.b = b;
		}

		public CssNthEvaluator(final int b) {
			this(0, b);
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			final Element p = element.parent();
			if (p == null || (p instanceof Document)) {
				return false;
			}

			final int pos = calculatePosition(root, element);
			if (a == 0) {
				return pos == b;
			}

			return (pos - b) * a >= 0 && (pos - b) % a == 0;
		}

		@Override
		public String toString() {
			if (a == 0) {
				return String.format(":%s(%d)", getPseudoClass(), b);
			}
			if (b == 0) {
				return String.format(":%s(%dn)", getPseudoClass(), a);
			}
			return String.format(":%s(%dn%+d)", getPseudoClass(), a, b);
		}

		protected abstract String getPseudoClass();

		protected abstract int calculatePosition(Element root, Element element);
	}

	/**
	 * css-compatible Evaluator for :eq (css :nth-child)
	 * 
	 * @see IndexEquals
	 */
	public static final class IsNthChild extends CssNthEvaluator {

		public IsNthChild(final int a, final int b) {
			super(a, b);
		}

		@Override
		protected int calculatePosition(final Element root, final Element element) {
			return element.elementSiblingIndex() + 1;
		}

		@Override
		protected String getPseudoClass() {
			return "nth-child";
		}
	}

	/**
	 * css pseudo class :nth-last-child)
	 * 
	 * @see IndexEquals
	 */
	public static final class IsNthLastChild extends CssNthEvaluator {
		public IsNthLastChild(final int a, final int b) {
			super(a, b);
		}

		@Override
		protected int calculatePosition(final Element root, final Element element) {
			return element.parent().children().size() - element.elementSiblingIndex();
		}

		@Override
		protected String getPseudoClass() {
			return "nth-last-child";
		}
	}

	/**
	 * css pseudo class nth-of-type
	 * 
	 */
	public static class IsNthOfType extends CssNthEvaluator {
		public IsNthOfType(final int a, final int b) {
			super(a, b);
		}

		@Override
		protected int calculatePosition(final Element root, final Element element) {
			int pos = 0;
			final Elements family = element.parent().children();
			for (int i = 0; i < family.size(); i++) {
				if (family.get(i).tag().equals(element.tag())) {
					pos++;
				}
				if (family.get(i) == element) {
					break;
				}
			}
			return pos;
		}

		@Override
		protected String getPseudoClass() {
			return "nth-of-type";
		}
	}

	public static class IsNthLastOfType extends CssNthEvaluator {

		public IsNthLastOfType(final int a, final int b) {
			super(a, b);
		}

		@Override
		protected int calculatePosition(final Element root, final Element element) {
			int pos = 0;
			final Elements family = element.parent().children();
			for (int i = element.elementSiblingIndex(); i < family.size(); i++) {
				if (family.get(i).tag().equals(element.tag())) {
					pos++;
				}
			}
			return pos;
		}

		@Override
		protected String getPseudoClass() {
			return "nth-last-of-type";
		}
	}

	/**
	 * Evaluator for matching the first sibling (css :first-child)
	 */
	public static final class IsFirstChild extends Evaluator {
		@Override
		public boolean matches(final Element root, final Element element) {
			final Element p = element.parent();
			return p != null && !(p instanceof Document) && element.elementSiblingIndex() == 0;
		}

		@Override
		public String toString() {
			return ":first-child";
		}
	}

	/**
	 * css3 pseudo-class :root
	 * 
	 * @see <a href="http://www.w3.org/TR/selectors/#root-pseudo">:root
	 *      selector</a>
	 *
	 */
	public static final class IsRoot extends Evaluator {
		@Override
		public boolean matches(final Element root, final Element element) {
			final Element r = root instanceof Document ? root.child(0) : root;
			return element == r;
		}

		@Override
		public String toString() {
			return ":root";
		}
	}

	public static final class IsOnlyChild extends Evaluator {
		@Override
		public boolean matches(final Element root, final Element element) {
			final Element p = element.parent();
			return p != null && !(p instanceof Document) && element.siblingElements().size() == 0;
		}

		@Override
		public String toString() {
			return ":only-child";
		}
	}

	public static final class IsOnlyOfType extends Evaluator {
		@Override
		public boolean matches(final Element root, final Element element) {
			final Element p = element.parent();
			if (p == null || p instanceof Document) {
				return false;
			}

			int pos = 0;
			final Elements family = p.children();
			for (int i = 0; i < family.size(); i++) {
				if (family.get(i).tag().equals(element.tag())) {
					pos++;
				}
			}
			return pos == 1;
		}

		@Override
		public String toString() {
			return ":only-of-type";
		}
	}

	public static final class IsEmpty extends Evaluator {
		@Override
		public boolean matches(final Element root, final Element element) {
			final List<Node> family = element.childNodes();
			for (int i = 0; i < family.size(); i++) {
				final Node n = family.get(i);
				if (!(n instanceof Comment || n instanceof XmlDeclaration || n instanceof DocumentType)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public String toString() {
			return ":empty";
		}
	}

	/**
	 * Abstract evaluator for sibling index matching
	 *
	 * @author ant
	 */
	public abstract static class IndexEvaluator extends Evaluator {
		int index;

		public IndexEvaluator(final int index) {
			this.index = index;
		}
	}

	/**
	 * Evaluator for matching Element (and its descendants) text
	 */
	public static final class ContainsText extends Evaluator {
		private final String searchText;

		public ContainsText(final String searchText) {
			this.searchText = searchText.toLowerCase();
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return (element.text().toLowerCase().contains(searchText));
		}

		@Override
		public String toString() {
			return String.format(":contains(%s", searchText);
		}
	}

	/**
	 * Evaluator for matching Element's own text
	 */
	public static final class ContainsOwnText extends Evaluator {
		private final String searchText;

		public ContainsOwnText(final String searchText) {
			this.searchText = searchText.toLowerCase();
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			return (element.ownText().toLowerCase().contains(searchText));
		}

		@Override
		public String toString() {
			return String.format(":containsOwn(%s", searchText);
		}
	}

	/**
	 * Evaluator for matching Element (and its descendants) text with regex
	 */
	public static final class Matches extends Evaluator {
		private final Pattern pattern;

		public Matches(final Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			final Matcher m = pattern.matcher(element.text());
			return m.find();
		}

		@Override
		public String toString() {
			return String.format(":matches(%s", pattern);
		}
	}

	/**
	 * Evaluator for matching Element's own text with regex
	 */
	public static final class MatchesOwn extends Evaluator {
		private final Pattern pattern;

		public MatchesOwn(final Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean matches(final Element root, final Element element) {
			final Matcher m = pattern.matcher(element.ownText());
			return m.find();
		}

		@Override
		public String toString() {
			return String.format(":matchesOwn(%s", pattern);
		}
	}
}

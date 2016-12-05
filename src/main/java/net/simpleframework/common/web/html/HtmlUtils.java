package net.simpleframework.common.web.html;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.nodes.Node;
import net.simpleframework.lib.org.jsoup.nodes.TextNode;
import net.simpleframework.lib.org.jsoup.parser.Parser;
import net.simpleframework.lib.org.jsoup.select.Elements;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class HtmlUtils implements HtmlConst {

	public static Document parseDocument(final String htmlString) {
		final Document document = Parser.parse(StringUtils.blank(htmlString), "");
		document.outputSettings().prettyPrint(false);
		return document;
	}

	public static Document createHtmlDocument(final String htmlString) {
		return createHtmlDocument(htmlString, "body");
	}

	public static Document createHtmlDocument(final String htmlString, final String tag) {
		final Document document = new Document("");
		final List<Node> nodeList = Parser.parseFragment(StringUtils.blank(htmlString),
				document.createElement(tag), "");
		for (final Node node : nodeList.toArray(new Node[nodeList.size()])) {
			document.appendChild(node);
		}
		document.outputSettings().prettyPrint(false);
		return document;
	}

	public static final String DEFAULT_NEW_LINE = "<br style=\"margin-bottom: 4px;\" />";

	public static String truncateHtml(final String htmlString, final int length) {
		return truncateHtml(htmlString, length, DEFAULT_NEW_LINE);
	}

	public static String truncateHtml(final String htmlString, final int length,
			final String newLine) {
		return truncateHtml(createHtmlDocument(htmlString), length, newLine);
	}

	public static String truncateHtml(final Document doc, final int length) {
		return truncateHtml(doc, length, DEFAULT_NEW_LINE);
	}

	public static String truncateHtml(final Document doc, final int length, final String newLine) {
		return truncateHtml(doc, length, newLine, true, false);
	}

	public static String truncateHtml(final Document doc, final int length, final String newLine,
			final boolean showLink, final boolean dot) {
		if (doc == null) {
			return null;
		}
		doc.attr("length", String.valueOf(length));
		String html = elementText(doc, doc.childNodes(), newLine, showLink);
		if (dot) {
			if (Convert.toInt(doc.attr("length")) <= 0) {
				html += "...";
			}
		}
		doc.removeAttr("length");
		doc.removeAttr("br");
		return html != null ? html.trim() : null;
	}

	// 0x3000 全角空格
	// 0xA0 Unicode编码后的non-breaking space
	private static final char[] BLANK_CHARs = { (char) 0x3000, (char) 0xA0 };

	private static String elementText(final Document doc, final List<Node> nodes,
			final String newLine, final boolean showLink) {
		final StringBuilder sb = new StringBuilder();
		for (final Node child : nodes) {
			final int length = Convert.toInt(doc.attr("length"));
			if (length <= 0) {
				break;
			}
			if (child instanceof TextNode) {
				String txt = ((TextNode) child).text();
				if (StringUtils.hasText(txt)) {
					for (final char c : BLANK_CHARs) {
						txt = txt.replace(c, ' ');
					}
					txt = StringUtils.substring(txt.trim(), length);
					sb.append(HtmlEncoder.text(txt));
					doc.attr("length", String.valueOf(length - txt.length()));
					doc.removeAttr("br");
				}
			} else if (child instanceof Element) {
				final Element element = (Element) child;
				final String tagName = element.tagName();
				String href;
				if (showLink && "a".equalsIgnoreCase(tagName) && element.children().size() == 0
						&& StringUtils.hasText(href = element.attr("href"))
						&& !href.toLowerCase().startsWith("javascript:")) {
					doc.attr("length", String.valueOf(length - element.text().length()));
					element.removeAttr("style").removeAttr("class").attr("target", "_blank");
					sb.append(element.outerHtml());
				} else if ("br".equalsIgnoreCase(tagName)) {
					if (StringUtils.hasText(newLine) && !doc.attr("br").equals("true")) {
						sb.append(newLine);
						doc.attr("br", "true");
					}
				} else {
					final String txt = elementText(doc, element.childNodes(), newLine, showLink);
					if (StringUtils.hasText(txt)) {
						sb.append(txt);
						if (StringUtils.hasText(newLine) && element.isBlock()
								&& Convert.toInt(doc.attr("length")) > 0
								&& !doc.attr("br").equals("true")) {
							sb.append(newLine);
							doc.attr("br", "true");
						}
					}
				}
			}
		}
		return sb.toString();
	}

	public static String htmlToText(final String htmlString) {
		return truncateHtml(htmlString, Integer.MAX_VALUE, "\n");
	}

	private static final Pattern EXPR_PATTERN = Pattern
			.compile("<script[^>]*>([\\S\\s]*?)</script>");

	public static String stripScripts(final String content) {
		if (StringUtils.hasText(content)) {
			return EXPR_PATTERN.matcher(content).replaceAll("");
		} else {
			return StringUtils.blank(content);
		}
	}

	public static final String convertHtmlLines(final String input) {
		return StringUtils
				.replace(StringUtils.replace(StringUtils.blank(input), "\n", "<br/>"), "\r", "")
				.replace("\t", NBSP + NBSP);
	}

	public static boolean hasTag(final String input) {
		for (final Object o : Parser.parseFragment(StringUtils.blank(input), new Document(""), "")) {
			if (!(o instanceof TextNode)) {
				return true;
			}
		}
		return false;
	}

	private static Pattern url_pattern;

	public static String autoLink(final String txt) {
		if (url_pattern == null) {
			url_pattern = Pattern.compile("(http(s)?|ftp)://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?");
		}
		final StringBuilder html = new StringBuilder();
		int lastIdx = 0;
		final Matcher matchr = url_pattern.matcher(txt);
		while (matchr.find()) {
			final String str = matchr.group();
			html.append(txt.substring(lastIdx, matchr.start()));
			html.append("<a target=\"_blank\" href=\"");
			html.append(str).append("\">").append(str).append("</a>");
			lastIdx = matchr.end();
		}
		html.append(txt.substring(lastIdx));
		return html.toString();
	}

	public static String tag(final String tagName, final Properties properties) {
		return tag(tagName, null, properties);
	}

	public static String tag(final String tagName, final String text, final Properties properties) {
		if (!StringUtils.hasText(tagName)) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		sb.append("<").append(tagName);
		if (properties != null) {
			for (final Entry<Object, Object> entry : properties.entrySet()) {
				sb.append(" ").append(entry.getKey()).append("=\"");
				sb.append(HtmlEncoder.text((String) entry.getValue()));
				sb.append("\"");
			}
		}
		if (text != null) {
			sb.append(">");
			sb.append(text).append("</").append(tagName).append(">");
		} else {
			sb.append("/>");
		}
		return sb.toString();
	}

	public static final String[] CONTEXTPATH_ATTRIBUTES = new String[] { "href", "src" };

	public static String stripContextPath(final HttpServletRequest httpRequest, final String html) {
		if (!StringUtils.hasText(httpRequest.getContextPath())) {
			return html;
		}
		return doDocument(HtmlUtils.createHtmlDocument(html), STRIP_CONTEXTPATH_VISITOR(httpRequest))
				.html();
	}

	public static String wrapContextPath(final HttpServletRequest httpRequest, final String html) {
		if (!StringUtils.hasText(httpRequest.getContextPath())) {
			return html;
		}
		return doDocument(HtmlUtils.createHtmlDocument(html), WRAP_CONTEXTPATH_VISITOR(httpRequest))
				.html();
	}

	public static Document doDocument(final Document doc, final IElementVisitor... visitors) {
		final Elements eles = doc.getAllElements();
		for (int i = 0; i < eles.size(); i++) {
			final Element ele = eles.get(i);
			for (final IElementVisitor visitor : visitors) {
				visitor.doElement(ele);
			}
		}
		return doc;
	}

	public static IElementVisitor STRIP_CONTEXTPATH_VISITOR(final HttpServletRequest httpRequest) {
		return new IElementVisitor() {
			@Override
			public void doElement(final Element ele) {
				for (final String attri : CONTEXTPATH_ATTRIBUTES) {
					final String sVal = ele.attr(attri);
					if (StringUtils.hasText(sVal) && !HttpUtils.isAbsoluteUrl(sVal)) {
						// 此处去掉ContextPath
						ele.attr(attri, HttpUtils.stripContextPath(httpRequest, sVal));
					}
				}
			}
		};
	}

	public static IElementVisitor WRAP_CONTEXTPATH_VISITOR(final HttpServletRequest httpRequest) {
		return new IElementVisitor() {
			@Override
			public void doElement(final Element ele) {
				for (final String attri : CONTEXTPATH_ATTRIBUTES) {
					final String sVal = ele.attr(attri);
					if (StringUtils.hasText(sVal)) {
						ele.attr(attri, HttpUtils.wrapContextPath(httpRequest, sVal));
					}
				}
			}
		};
	}

	public static IElementVisitor REMOVE_TAG_VISITOR(final String tag) {
		return REMOVE_TAG_VISITOR(tag, false);
	}

	public static IElementVisitor REMOVE_TAG_VISITOR(final String tag, final boolean unwrap) {
		return new IElementVisitor() {
			@Override
			public void doElement(final Element ele) {
				if (tag.equalsIgnoreCase(ele.tagName())) {
					if (unwrap) {
						ele.unwrap();
					} else {
						ele.remove();
					}
				}
			}
		};
	}

	public static IElementVisitor REPLACE_TAG_VISITOR(final String tag, final String tag2) {
		return new IElementVisitor() {
			@Override
			public void doElement(final Element ele) {
				if (tag.equalsIgnoreCase(ele.tagName())) {
					ele.tagName(tag2);
				}
			}
		};
	}

	public static IElementVisitor REMOVE_ATTRI_VISITOR(final String... attris) {
		return REMOVE_ATTRI_VISITOR(null, attris);
	}

	public static IElementVisitor REMOVE_ATTRI_VISITOR(final String tag, final String[] attris) {
		return new IElementVisitor() {
			@Override
			public void doElement(final Element ele) {
				if (attris != null && (tag == null || tag.equalsIgnoreCase(ele.tagName()))) {
					for (final String attri : attris) {
						ele.removeAttr(attri);
					}
				}
			}
		};
	}

	public static IElementVisitor ADD_ATTRI_VISITOR(final String tag, final String key,
			final Object val) {
		return ADD_ATTRI_VISITOR(tag, new KVMap().add(key, val));
	}

	public static IElementVisitor ADD_ATTRI_VISITOR(final String tag, final KVMap attris) {
		return new IElementVisitor() {
			@Override
			public void doElement(final Element ele) {
				if (attris != null && (tag == null || tag.equalsIgnoreCase(ele.tagName()))) {
					for (final Map.Entry<String, Object> entry : attris.entrySet()) {
						ele.attr(entry.getKey(), Convert.toString(entry.getValue()));
					}
				}
			}
		};
	}

	public static IElementVisitor TARGET_BLANK_VISITOR = new IElementVisitor() {
		@Override
		public void doElement(final Element ele) {
			if ("a".equalsIgnoreCase(ele.tagName())) {
				final String href = ele.attr("href");
				if (href != null && !href.toLowerCase().startsWith("javascript:")) {
					ele.attr("target", "_blank");
				}
			}
		}
	};

	public static interface IElementVisitor {

		void doElement(Element ele);
	}

	static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*?>.*?<\\/script>");

	public static String trimScript(final String str) {
		if (!StringUtils.hasText(str)) {
			return str;
		}
		final Matcher m = SCRIPT_PATTERN.matcher(str);
		return m.replaceAll("");
	}

	public static String escapeScript(final String str) {
		if (!StringUtils.hasText(str)) {
			return str;
		}
		final Matcher m = SCRIPT_PATTERN.matcher(str);
		int i = 0;
		final StringBuilder sb = new StringBuilder();
		while (m.find()) {
			final int s = m.start();
			final int e = m.end();
			sb.append(str.substring(i, s));
			sb.append(HtmlEncoder.text(str.substring(s, e)));
			i = e;
		}
		if (i == 0) {
			return str;
		}
		return sb.append(str.substring(i)).toString();
	}
}

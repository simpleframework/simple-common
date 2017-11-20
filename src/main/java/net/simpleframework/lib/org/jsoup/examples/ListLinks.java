package net.simpleframework.lib.org.jsoup.examples;

import java.io.IOException;

import net.simpleframework.lib.org.jsoup.Jsoup;
import net.simpleframework.lib.org.jsoup.helper.Validate;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.select.Elements;

/**
 * Example program to list links from a URL.
 */
public class ListLinks {
	public static void main(final String[] args) throws IOException {
		Validate.isTrue(args.length == 1, "usage: supply url to fetch");
		final String url = args[0];
		print("Fetching %s...", url);

		final Document doc = Jsoup.connect(url).get();
		final Elements links = doc.select("a[href]");
		final Elements media = doc.select("[src]");
		final Elements imports = doc.select("link[href]");

		print("\nMedia: (%d)", media.size());
		for (final Element src : media) {
			if (src.tagName().equals("img")) {
				print(" * %s: <%s> %sx%s (%s)", src.tagName(), src.attr("abs:src"), src.attr("width"),
						src.attr("height"), trim(src.attr("alt"), 20));
			} else {
				print(" * %s: <%s>", src.tagName(), src.attr("abs:src"));
			}
		}

		print("\nImports: (%d)", imports.size());
		for (final Element link : imports) {
			print(" * %s <%s> (%s)", link.tagName(), link.attr("abs:href"), link.attr("rel"));
		}

		print("\nLinks: (%d)", links.size());
		for (final Element link : links) {
			print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
		}
	}

	private static void print(final String msg, final Object... args) {
		System.out.println(String.format(msg, args));
	}

	private static String trim(final String s, final int width) {
		if (s.length() > width) {
			return s.substring(0, width - 1) + ".";
		} else {
			return s;
		}
	}
}

package net.simpleframework.lib.org.jsoup.examples;

import java.io.IOException;

import net.simpleframework.lib.org.jsoup.Jsoup;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.select.Elements;

/**
 * A simple example, used on the jsoup website.
 */
public class Wikipedia {
	public static void main(final String[] args) throws IOException {
		final Document doc = Jsoup.connect("http://en.wikipedia.org/").get();
		log(doc.title());

		final Elements newsHeadlines = doc.select("#mp-itn b a");
		for (final Element headline : newsHeadlines) {
			log("%s\n\t%s", headline.attr("title"), headline.absUrl("href"));
		}
	}

	private static void log(final String msg, final String... vals) {
		System.out.println(String.format(msg, vals));
	}
}

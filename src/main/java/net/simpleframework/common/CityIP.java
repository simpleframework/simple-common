package net.simpleframework.common;

import java.io.IOException;
import java.net.URL;

import net.simpleframework.common.coll.LRUMap;
import net.simpleframework.lib.org.jsoup.Jsoup;
import net.simpleframework.lib.org.jsoup.nodes.Element;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public abstract class CityIP {
	private static LRUMap<String, String> ipCache = new LRUMap<String, String>(1000);

	public static String getCity(final String ip, final boolean more) throws IOException {
		if ("127.0.0.1".equals(ip) || "localhost".equals(ip)) {
			return "";
		}

		String city = ipCache.get(ip);
		if (city != null) {
			if (!more) {
				final int e = city.indexOf(" ");
				if (e > 0) {
					return city.substring(0, e);
				}
			}
			return city;
		}
		try {
			// http://ip.taobao.com/service/getIpInfo.php?ip=
			final URL url = new URL("http://www.cz88.net/ip/?ip=" + ip);
			final String htmlString = IoUtils.getStringFromInputStream(url.openStream(), "gbk");
			final Element first = Jsoup.parse(htmlString).select("#InputIPAddrMessage").first();
			if (first != null) {
				ipCache.put(ip, city = first.text());
				return city;
			}
		} catch (final IOException e) {
		}
		return "";
	}
}

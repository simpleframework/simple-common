package net.simpleframework.common.web;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.object.ObjectEx;
import net.simpleframework.lib.org.jsoup.Connection;
import net.simpleframework.lib.org.jsoup.Connection.Method;
import net.simpleframework.lib.org.jsoup.Jsoup;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class HttpClient extends ObjectEx {
	private String url;

	private String jsessionid;

	public HttpClient(final String url) {
		setUrl(url);
	}

	public String getUrl() {
		return url;
	}

	public HttpClient setUrl(final String url) {
		if (url != null) {
			this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
		}
		return this;
	}

	public String getJsessionid() {
		return jsessionid;
	}

	public HttpClient setJsessionid(final String jsessionid) {
		this.jsessionid = jsessionid;
		return this;
	}

	/** --------------- get --------------- **/

	public Map<String, Object> get(final String path, final Map<String, Object> data)
			throws IOException {
		return new KVMap(getResponseText(path, data, false));
	}

	public Map<String, Object> get(final String path) throws IOException {
		return get(path, null);
	}

	/** --------------- post --------------- **/

	public Map<String, Object> post(final String path, final Map<String, Object> data)
			throws IOException {
		return new KVMap(getResponseText(path, data, true));
	}

	public String text() throws IOException {
		return getResponseText(null, null, false);
	}

	protected String getResponseText(final String path, final Map<String, Object> data,
			final boolean post) throws IOException {
		String url = getUrl();
		if (!url.toLowerCase().startsWith("http://")) {
			url = "http://" + url;
		}
		if (StringUtils.hasText(path)) {
			url += path;
		}

		final String jsessionid = getJsessionid();
		if (StringUtils.hasText(jsessionid)) {
			final int p = url.indexOf("?");
			if (p > 0) {
				url = url.substring(0, p) + ";jsessionid=" + jsessionid + url.substring(p);
			} else {
				url = url + ";jsessionid=" + jsessionid;
			}
		}
		final Connection conn = Jsoup.connect(url).userAgent("HttpClient-[service]").timeout(0);
		if (StringUtils.hasText(jsessionid)) {
			conn.cookie("jsessionid", jsessionid);
		}
		if (data != null) {
			for (final Map.Entry<String, Object> o : data.entrySet()) {
				conn.data(o.getKey(), String.valueOf(o.getValue()));
			}
		}
		if (post) {
			conn.method(Method.POST);
		}
		return conn.execute().body();
	}
}

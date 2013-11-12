package net.simpleframework.lib.org.jsoup;

import java.io.IOException;

/**
 * Signals that a HTTP request resulted in a not OK HTTP response.
 */
public class HttpStatusException extends IOException {
	private final int statusCode;
	private final String url;

	public HttpStatusException(final String message, final int statusCode, final String url) {
		super(message);
		this.statusCode = statusCode;
		this.url = url;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return super.toString() + ". Status=" + statusCode + ", URL=" + url;
	}
}

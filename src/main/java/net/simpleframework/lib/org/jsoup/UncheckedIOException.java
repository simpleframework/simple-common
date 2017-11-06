package net.simpleframework.lib.org.jsoup;

import java.io.IOException;

public class UncheckedIOException extends Error {
	public UncheckedIOException(final IOException cause) {
		super(cause);
	}

	public IOException ioException() {
		return (IOException) getCause();
	}
}

package net.simpleframework.lib.org.jsoup;

import java.io.IOException;

public class UncheckedIOException extends RuntimeException {
	public UncheckedIOException(final IOException cause) {
		super(cause);
	}

	public UncheckedIOException(final String message) {
		super(new IOException(message));
	}

	public IOException ioException() {
		return (IOException) getCause();
	}
}
